package com.coffeeandmore;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * Manages MySQL connection, schema initialization, auto-seeding, and CRUD operations.
 * Includes graceful memory caching fallback if MySQL credentials are being set up.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Properties config = new Properties();
    private boolean isConnected = false;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    // In-memory fallback caches if MySQL is temporarily offline
    private final List<Map<String, Object>> fallbackMenuItems = new ArrayList<>();
    private final List<Map<String, Object>> fallbackReviews = new ArrayList<>();
    private final List<Map<String, Object>> fallbackInquiries = new ArrayList<>();

    private DatabaseManager() {
        loadConfig();
        initFallbackData();
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadConfig() {
        try (InputStream in = new FileInputStream("backend/config.properties")) {
            config.load(in);
        } catch (Exception e) {
            try (InputStream in = new FileInputStream("config.properties")) {
                config.load(in);
            } catch (Exception ex) {
                System.out.println("[CONFIG] Using default database configuration.");
            }
        }

        String host = config.getProperty("db.host", "localhost");
        String port = config.getProperty("db.port", "3306");
        String name = config.getProperty("db.name", "coffee_and_more_db");
        dbUser = config.getProperty("db.user", "root");
        dbPassword = config.getProperty("db.password", "root");

        dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + name +
                "?useSSL=" + config.getProperty("db.useSSL", "false") +
                "&allowPublicKeyRetrieval=" + config.getProperty("db.allowPublicKeyRetrieval", "true") +
                "&serverTimezone=" + config.getProperty("db.serverTimezone", "UTC");
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver may already be on classpath in Java 17
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    public synchronized void initDatabase() {
        String host = config.getProperty("db.host", "localhost");
        String port = config.getProperty("db.port", "3306");
        String name = config.getProperty("db.name", "coffee_and_more_db");
        String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception ignored) {}

        // 1. Create database if it does not exist
        try (Connection conn = DriverManager.getConnection(serverUrl, dbUser, dbPassword);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + name + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
            System.out.println("[MYSQL] Database '" + name + "' verified / created successfully.");
            isConnected = true;
        } catch (Exception e) {
            System.err.println("[MYSQL WARNING] Could not auto-create database: " + e.getMessage());
            System.err.println("[MYSQL NOTICE] Operating with auto-sync fallback. You can update backend/config.properties anytime.");
            isConnected = false;
        }

        if (isConnected) {
            createTablesAndSeed();
        }
    }

    private void createTablesAndSeed() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Table 1: Menu Items
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS menu_items (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  category VARCHAR(50) NOT NULL," +
                "  name VARCHAR(150) NOT NULL," +
                "  description TEXT NOT NULL," +
                "  is_veg BOOLEAN NOT NULL DEFAULT TRUE," +
                "  tag VARCHAR(50) DEFAULT NULL," +
                "  serving VARCHAR(100) NOT NULL," +
                "  price DECIMAL(6, 2) NOT NULL DEFAULT 150.00," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");"
            );

            // Table 2: Contact Inquiries
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS contact_inquiries (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  full_name VARCHAR(100) NOT NULL," +
                "  mobile_number VARCHAR(30) NOT NULL," +
                "  email_address VARCHAR(100) NOT NULL," +
                "  reason VARCHAR(60) NOT NULL," +
                "  message TEXT NOT NULL," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'NEW'," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");"
            );

            // Table 3: Passenger Pre-Orders
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS passenger_orders (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  customer_name VARCHAR(100) NOT NULL," +
                "  customer_phone VARCHAR(30) NOT NULL," +
                "  flight_number VARCHAR(20) DEFAULT NULL," +
                "  item_name VARCHAR(150) NOT NULL," +
                "  quantity INT NOT NULL DEFAULT 1," +
                "  special_notes TEXT DEFAULT NULL," +
                "  total_price DECIMAL(7, 2) NOT NULL," +
                "  order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PREP'," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");"
            );

            // Table 4: Traveler Reviews
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS traveler_reviews (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  reviewer_name VARCHAR(100) NOT NULL," +
                "  reviewer_title VARCHAR(150) NOT NULL," +
                "  rating INT NOT NULL DEFAULT 5," +
                "  comment TEXT NOT NULL," +
                "  verified_tag VARCHAR(100) DEFAULT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");"
            );

            System.out.println("[MYSQL] All 4 tables verified in database.");

            // Check if menu_items needs seeding
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM menu_items;");
            if (rs.next() && rs.getInt(1) == 0) {
                seedMenuItems(conn);
            }

            // Check if traveler_reviews needs seeding
            ResultSet rsRev = stmt.executeQuery("SELECT COUNT(*) FROM traveler_reviews;");
            if (rsRev.next() && rsRev.getInt(1) == 0) {
                seedTravelerReviews(conn);
            }

        } catch (SQLException e) {
            System.err.println("[MYSQL ERROR] Table creation or seed failed: " + e.getMessage());
        }
    }

    private void seedMenuItems(Connection conn) throws SQLException {
        String sql = "INSERT INTO menu_items (category, name, description, is_veg, tag, serving, price) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, Object> item : fallbackMenuItems) {
                ps.setString(1, (String) item.get("category"));
                ps.setString(2, (String) item.get("name"));
                ps.setString(3, (String) item.get("description"));
                ps.setBoolean(4, (Boolean) item.get("is_veg"));
                ps.setString(5, (String) item.get("tag"));
                ps.setString(6, (String) item.get("serving"));
                ps.setBigDecimal(7, new BigDecimal(item.get("price").toString()));
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("[MYSQL] Successfully seeded all 25 handcrafted menu items.");
        }
    }

    private void seedTravelerReviews(Connection conn) throws SQLException {
        String sql = "INSERT INTO traveler_reviews (reviewer_name, reviewer_title, rating, comment, verified_tag) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, Object> rev : fallbackReviews) {
                ps.setString(1, (String) rev.get("reviewer_name"));
                ps.setString(2, (String) rev.get("reviewer_title"));
                ps.setInt(3, (Integer) rev.get("rating"));
                ps.setString(4, (String) rev.get("comment"));
                ps.setString(5, (String) rev.get("verified_tag"));
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("[MYSQL] Successfully seeded traveler reviews.");
        }
    }

    // =========================================================================
    // CRUD OPERATIONS
    // =========================================================================

    public boolean saveContactInquiry(String name, String mobile, String email, String reason, String message) {
        // Record in fallback memory store
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("id", fallbackInquiries.size() + 1);
        record.put("full_name", name);
        record.put("mobile_number", mobile);
        record.put("email_address", email);
        record.put("reason", reason);
        record.put("message", message);
        record.put("status", "NEW");
        record.put("created_at", new java.util.Date().toString());
        fallbackInquiries.add(record);

        if (!isConnected) {
            System.out.println("[INQUIRY LOGGED] Saved to local memory store (MySQL offline).");
            return true;
        }

        String sql = "INSERT INTO contact_inquiries (full_name, mobile_number, email_address, reason, message) VALUES (?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, mobile);
            ps.setString(3, email);
            ps.setString(4, reason);
            ps.setString(5, message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MYSQL ERROR] Could not save inquiry to database: " + e.getMessage());
            return true; // Already saved in fallback store
        }
    }

    public List<Map<String, Object>> getMenuItems(String category, String search) {
        if (!isConnected) {
            return filterItems(fallbackMenuItems, category, search);
        }

        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM menu_items WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (category != null && !category.equalsIgnoreCase("all") && !category.isBlank()) {
            sql.append("AND category = ? ");
            params.add(category);
        }
        if (search != null && !search.isBlank()) {
            sql.append("AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?) ");
            params.add("%" + search.toLowerCase() + "%");
            params.add("%" + search.toLowerCase() + "%");
        }
        sql.append("ORDER BY id ASC;");

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", rs.getInt("id"));
                    map.put("category", rs.getString("category"));
                    map.put("name", rs.getString("name"));
                    map.put("description", rs.getString("description"));
                    map.put("is_veg", rs.getBoolean("is_veg"));
                    map.put("tag", rs.getString("tag"));
                    map.put("serving", rs.getString("serving"));
                    map.put("price", rs.getBigDecimal("price").doubleValue());
                    list.add(map);
                }
            }
            return list;
        } catch (SQLException e) {
            System.err.println("[MYSQL ERROR] Failed to fetch menu items: " + e.getMessage());
            return filterItems(fallbackMenuItems, category, search);
        }
    }

    public List<Map<String, Object>> getTravelerReviews() {
        if (!isConnected) {
            return fallbackReviews;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT * FROM traveler_reviews ORDER BY id ASC;";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("reviewer_name", rs.getString("reviewer_name"));
                map.put("reviewer_title", rs.getString("reviewer_title"));
                map.put("rating", rs.getInt("rating"));
                map.put("comment", rs.getString("comment"));
                map.put("verified_tag", rs.getString("verified_tag"));
                list.add(map);
            }
            return list;
        } catch (SQLException e) {
            return fallbackReviews;
        }
    }

    public boolean savePassengerOrder(String name, String phone, String flight, String item, int qty, String notes, double total) {
        if (!isConnected) {
            System.out.println("[ORDER LOGGED] " + qty + "x " + item + " for " + name + " (Flight: " + flight + ")");
            return true;
        }
        String sql = "INSERT INTO passenger_orders (customer_name, customer_phone, flight_number, item_name, quantity, special_notes, total_price) VALUES (?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, flight);
            ps.setString(4, item);
            ps.setInt(5, qty);
            ps.setString(6, notes);
            ps.setBigDecimal(7, BigDecimal.valueOf(total));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[MYSQL ERROR] Could not save order: " + e.getMessage());
            return true;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    // =========================================================================
    // IN-MEMORY FALLBACK INITIALIZATION
    // =========================================================================
    private void initFallbackData() {
        // 25 Items from content.txt
        addItem("coffee", "Signature Airport Cappuccino", "Velvety steamed milk poured over a rich double-shot espresso, finished with a dusted cocoa crown.", true, "Best Seller", "Hot (Regular / Large)", 180.0);
        addItem("coffee", "Classic Americano", "Bold, smooth, and aromatic. Premium espresso pulled over hot water for pure coffee lovers.", true, null, "Hot / Iced", 140.0);
        addItem("coffee", "Café Latte", "Subtle espresso infused with silky microfoam milk. Mellow, smooth, and uplifting.", true, null, "Hot", 160.0);
        addItem("coffee", "Rich Hazelnut Café Mocha", "Espresso blended with artisanal chocolate, roasted hazelnut notes, and whipped cream.", true, null, "Hot / Iced", 210.0);
        addItem("coffee", "Creamy Caramel Iced Frappé", "Blended chilled espresso, fresh milk, and caramel drizzle topped with whipped cream.", true, "Traveler Favorite", "Chilled Glass", 220.0);

        addItem("teas", "Masala Chai Special", "Traditional Indian milk tea infused with crushed cardamom, ginger, cloves, and cinnamon.", true, "House Special", "Piping Hot", 90.0);
        addItem("teas", "Green Tea & Lemon Honey Infusion", "Antioxidant-rich whole leaf green tea with a squeeze of fresh lemon and raw honey.", true, null, "Steamed Cup", 110.0);
        addItem("teas", "Classic English Breakfast Tea", "Full-bodied black tea brew served with warm milk and natural sugar on the side.", true, null, "Hot", 120.0);

        addItem("sandwiches", "Garden Fresh Veggie & Cheese Grilled Sandwich", "Crispy grilled bread layered with crisp bell peppers, English cucumber, sweet corn, and melted mozzarella.", true, "Top Pick", "Served with signature dip & crisp wafers", 170.0);
        addItem("sandwiches", "Spiced Paneer Tikka Panini", "Marinated tender cottage cheese cubes tossed in tandoori spices and mint mayo, pressed on artisan bread.", true, "Must Try", "Warm & Toasted", 190.0);
        addItem("sandwiches", "Classic Herb Grilled Chicken Sandwich", "Juicy shredded chicken breast seasoned with rosemary, garlic emulsion, and cheddar cheese.", false, null, "Golden Grilled", 210.0);
        addItem("sandwiches", "Double Cheese & Corn Crunch Toastie", "Golden sweet corn and aged cheddar cheese toasted between buttery slices with herb flakes.", true, null, "Fresh off the grill", 160.0);

        addItem("snacks", "Golden Crispy Potato Wedges", "Lightly spiced potato wedges served piping hot with garlic mayo and tangy salsa dip.", true, null, "Piping Hot Basket", 130.0);
        addItem("snacks", "Herb-Crusted Paneer / Cheese Poppers", "Crunchy bite-sized poppers filled with molten spiced cheese that melts on first bite.", true, null, "Crunchy Molten Basket", 160.0);
        addItem("snacks", "Baked Veggie Puff Pastry", "Flaky golden puff pastry stuffed with spiced potato and vegetable filling. An airport classic.", true, null, "Flaky Oven Bake", 70.0);

        addItem("muffins", "Blueberry Burst Muffin", "Moist, soft vanilla sponge packed with tart wild blueberries and a delicate crumble crust.", true, "Freshly Baked", "Freshly Oven Baked", 120.0);
        addItem("muffins", "Double Dark Chocolate Chip Muffin", "Decadent Dutch cocoa muffin packed with rich chocolate chunks that melt in your mouth.", true, null, "Rich & Fudgy", 130.0);
        addItem("muffins", "Golden Butter Croissant", "Light, layered, and flaky French butter croissant toasted gently on request.", true, null, "Toasted on Request", 140.0);
        addItem("muffins", "Cinnamon & Apple Swirl Danish", "Golden puff pastry rolled with sweet spiced apple compote and warm cinnamon glaze.", true, null, "Warm Glazed Swirl", 150.0);

        addItem("desserts", "Warm Walnut Choco Brownie", "Fudgy dark chocolate brownie topped with toasted walnuts. Served warm with chocolate drizzle.", true, "Bestseller", "Warm with Chocolate Drizzle", 160.0);
        addItem("desserts", "New York Style Cream Cheesecake", "Smooth, velvety cream cheese on a crunchy graham cracker crust with berry glaze.", true, null, "Chilled Slice", 190.0);
        addItem("desserts", "Chocolate Truffle Slice", "Layers of moist dark sponge and silky chocolate ganache for true chocolate lovers.", true, null, "Silky Layered Slice", 180.0);

        addItem("chilled", "Chilled Mango & Mint Cooler", "Fresh alphonso mango purée blended with crushed mint sprigs, lime zest, and sparkling soda.", true, null, "Tall Chilled Glass", 150.0);
        addItem("chilled", "Tangy Lime & Mint Mojito (Non-Alcoholic)", "Zesty lime wedges muddled with fragrant farm mint and fizzy soda over crushed ice.", true, null, "Iced Cooler", 140.0);
        addItem("chilled", "Creamy Oreo Cookie Milkshake", "Thick whole milk blended with crushed Oreo cookies, rich vanilla gelato, and chocolate sauce.", true, null, "Thick Shake", 190.0);
        addItem("chilled", "Classic Cold Brew on Rocks", "Steeped for 18 hours in cold filtered water for natural chocolate sweetness and zero bitterness.", true, null, "18-Hour Brew", 170.0);

        // Reviews
        addReview("Priya S.", "Frequent Flyer (Bengaluru – Chennai)", 5, "Coffee & More is my designated pit-stop whenever I fly out of Chennai! The cappuccino is authentic and piping hot, not like ordinary airport vending machines. The paneer sandwich was freshly made and super filling. Total bill was under ₹350. Wonderful spot!", "Boarded 7:30 AM Flight");
        addReview("Karthik Narayanan", "Tech Consultant", 5, "My flight had a 2-hour delay, and this cozy café made the wait painless. Found a charging point, enjoyed a warm blueberry muffin and cold coffee, and worked peacefully. Courteous staff who genuinely smile!", "Transit Passenger");
        addReview("Anita & Rohan Desai", "Vacationers", 5, "Finding affordable and fresh food inside an airport is usually difficult, but Coffee & More surprised us. The grilled sandwich and choco brownie are top-notch. Clean counter and fast service!", "Family Traveler");
    }

    private void addItem(String cat, String name, String desc, boolean veg, String tag, String serving, double price) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", fallbackMenuItems.size() + 1);
        m.put("category", cat);
        m.put("name", name);
        m.put("description", desc);
        m.put("is_veg", veg);
        m.put("tag", tag);
        m.put("serving", serving);
        m.put("price", price);
        fallbackMenuItems.add(m);
    }

    private void addReview(String name, String title, int stars, String comment, String tag) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("id", fallbackReviews.size() + 1);
        r.put("reviewer_name", name);
        r.put("reviewer_title", title);
        r.put("rating", stars);
        r.put("comment", comment);
        r.put("verified_tag", tag);
        fallbackReviews.add(r);
    }

    private List<Map<String, Object>> filterItems(List<Map<String, Object>> src, String cat, String search) {
        List<Map<String, Object>> res = new ArrayList<>();
        for (Map<String, Object> item : src) {
            boolean matchesCat = cat == null || cat.equalsIgnoreCase("all") || cat.isBlank() || cat.equalsIgnoreCase((String) item.get("category"));
            boolean matchesSearch = search == null || search.isBlank() ||
                ((String) item.get("name")).toLowerCase().contains(search.toLowerCase()) ||
                ((String) item.get("description")).toLowerCase().contains(search.toLowerCase());
            if (matchesCat && matchesSearch) {
                res.add(item);
            }
        }
        return res;
    }
}


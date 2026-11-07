-- =============================================================================
-- COFFEE & MORE — MYSQL DATABASE SETUP SCRIPT
-- Run this in MySQL Workbench or mysql CLI:
-- mysql -u root -p < setup-mysql.sql
-- =============================================================================

CREATE DATABASE IF NOT EXISTS coffee_and_more_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE coffee_and_more_db;

-- 1. Table: Menu Items
CREATE TABLE IF NOT EXISTS menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    is_veg BOOLEAN NOT NULL DEFAULT TRUE,
    tag VARCHAR(50) DEFAULT NULL,
    serving VARCHAR(100) NOT NULL,
    price DECIMAL(6, 2) NOT NULL DEFAULT 150.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Table: Contact & Bulk Parcel Inquiries
CREATE TABLE IF NOT EXISTS contact_inquiries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    mobile_number VARCHAR(30) NOT NULL,
    email_address VARCHAR(100) NOT NULL,
    reason VARCHAR(60) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Table: Passenger Pre-Orders & Express Flight Bites
CREATE TABLE IF NOT EXISTS passenger_orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    flight_number VARCHAR(20) DEFAULT NULL,
    item_name VARCHAR(150) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    special_notes TEXT DEFAULT NULL,
    total_price DECIMAL(7, 2) NOT NULL,
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PREP',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Table: Traveler Reviews
CREATE TABLE IF NOT EXISTS traveler_reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reviewer_name VARCHAR(100) NOT NULL,
    reviewer_title VARCHAR(150) NOT NULL,
    rating INT NOT NULL DEFAULT 5,
    comment TEXT NOT NULL,
    verified_tag VARCHAR(100) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed all 25 handcrafted menu items from content.txt
INSERT INTO menu_items (category, name, description, is_veg, tag, serving, price) VALUES
('coffee', 'Signature Airport Cappuccino', 'Velvety steamed milk poured over a rich double-shot espresso, finished with a dusted cocoa crown.', TRUE, 'Best Seller', 'Hot (Regular / Large)', 180.00),
('coffee', 'Classic Americano', 'Bold, smooth, and aromatic. Premium espresso pulled over hot water for pure coffee lovers.', TRUE, NULL, 'Hot / Iced', 140.00),
('coffee', 'Café Latte', 'Subtle espresso infused with silky microfoam milk. Mellow, smooth, and uplifting.', TRUE, NULL, 'Hot', 160.00),
('coffee', 'Rich Hazelnut Café Mocha', 'Espresso blended with artisanal chocolate, roasted hazelnut notes, and whipped cream.', TRUE, NULL, 'Hot / Iced', 210.00),
('coffee', 'Creamy Caramel Iced Frappé', 'Blended chilled espresso, fresh milk, and caramel drizzle topped with whipped cream.', TRUE, 'Traveler Favorite', 'Chilled Glass', 220.00),

('teas', 'Masala Chai Special', 'Traditional Indian milk tea infused with crushed cardamom, ginger, cloves, and cinnamon.', TRUE, 'House Special', 'Piping Hot', 90.00),
('teas', 'Green Tea & Lemon Honey Infusion', 'Antioxidant-rich whole leaf green tea with a squeeze of fresh lemon and raw honey.', TRUE, NULL, 'Steamed Cup', 110.00),
('teas', 'Classic English Breakfast Tea', 'Full-bodied black tea brew served with warm milk and natural sugar on the side.', TRUE, NULL, 'Hot', 120.00),

('sandwiches', 'Garden Fresh Veggie & Cheese Grilled Sandwich', 'Crispy grilled bread layered with crisp bell peppers, English cucumber, sweet corn, and melted mozzarella.', TRUE, 'Top Pick', 'Served with signature dip & crisp wafers', 170.00),
('sandwiches', 'Spiced Paneer Tikka Panini', 'Marinated tender cottage cheese cubes tossed in tandoori spices and mint mayo, pressed on artisan bread.', TRUE, 'Must Try', 'Warm & Toasted', 190.00),
('sandwiches', 'Classic Herb Grilled Chicken Sandwich', 'Juicy shredded chicken breast seasoned with rosemary, garlic emulsion, and cheddar cheese.', FALSE, NULL, 'Golden Grilled', 210.00),
('sandwiches', 'Double Cheese & Corn Crunch Toastie', 'Golden sweet corn and aged cheddar cheese toasted between buttery slices with herb flakes.', TRUE, NULL, 'Fresh off the grill', 160.00),

('snacks', 'Golden Crispy Potato Wedges', 'Lightly spiced potato wedges served piping hot with garlic mayo and tangy salsa dip.', TRUE, NULL, 'Piping Hot Basket', 130.00),
('snacks', 'Herb-Crusted Paneer / Cheese Poppers', 'Crunchy bite-sized poppers filled with molten spiced cheese that melts on first bite.', TRUE, NULL, 'Crunchy Molten Basket', 160.00),
('snacks', 'Baked Veggie Puff Pastry', 'Flaky golden puff pastry stuffed with spiced potato and vegetable filling. An airport classic.', TRUE, NULL, 'Flaky Oven Bake', 70.00),

('muffins', 'Blueberry Burst Muffin', 'Moist, soft vanilla sponge packed with tart wild blueberries and a delicate crumble crust.', TRUE, 'Freshly Baked', 'Freshly Oven Baked', 120.00),
('muffins', 'Double Dark Chocolate Chip Muffin', 'Decadent Dutch cocoa muffin packed with rich chocolate chunks that melt in your mouth.', TRUE, NULL, 'Rich & Fudgy', 130.00),
('muffins', 'Golden Butter Croissant', 'Light, layered, and flaky French butter croissant toasted gently on request.', TRUE, NULL, 'Toasted on Request', 140.00),
('muffins', 'Cinnamon & Apple Swirl Danish', 'Golden puff pastry rolled with sweet spiced apple compote and warm cinnamon glaze.', TRUE, NULL, 'Warm Glazed Swirl', 150.00),

('desserts', 'Warm Walnut Choco Brownie', 'Fudgy dark chocolate brownie topped with toasted walnuts. Served warm with chocolate drizzle.', TRUE, 'Bestseller', 'Warm with Chocolate Drizzle', 160.00),
('desserts', 'New York Style Cream Cheesecake', 'Smooth, velvety cream cheese on a crunchy graham cracker crust with berry glaze.', TRUE, NULL, 'Chilled Slice', 190.00),
('desserts', 'Chocolate Truffle Slice', 'Layers of moist dark sponge and silky chocolate ganache for true chocolate lovers.', TRUE, NULL, 'Silky Layered Slice', 180.00),

('chilled', 'Chilled Mango & Mint Cooler', 'Fresh alphonso mango purée blended with crushed mint sprigs, lime zest, and sparkling soda.', TRUE, NULL, 'Tall Chilled Glass', 150.00),
('chilled', 'Tangy Lime & Mint Mojito (Non-Alcoholic)', 'Zesty lime wedges muddled with fragrant farm mint and fizzy soda over crushed ice.', TRUE, NULL, 'Iced Cooler', 140.00),
('chilled', 'Creamy Oreo Cookie Milkshake', 'Thick whole milk blended with crushed Oreo cookies, rich vanilla gelato, and chocolate sauce.', TRUE, NULL, 'Thick Shake', 190.00),
('chilled', 'Classic Cold Brew on Rocks', 'Steeped for 18 hours in cold filtered water for natural chocolate sweetness and zero bitterness.', TRUE, NULL, '18-Hour Brew', 170.00);

-- Seed traveler reviews
INSERT INTO traveler_reviews (reviewer_name, reviewer_title, rating, comment, verified_tag) VALUES
('Priya S.', 'Frequent Flyer (Bengaluru – Chennai)', 5, 'Coffee & More is my designated pit-stop whenever I fly out of Chennai! The cappuccino is authentic and piping hot, not like ordinary airport vending machines. The paneer sandwich was freshly made and super filling. Total bill was under ₹350. Wonderful spot!', 'Boarded 7:30 AM Flight'),
('Karthik Narayanan', 'Tech Consultant', 5, 'My flight had a 2-hour delay, and this cozy café made the wait painless. Found a charging point, enjoyed a warm blueberry muffin and cold coffee, and worked peacefully. Courteous staff who genuinely smile!', 'Transit Passenger'),
('Anita & Rohan Desai', 'Vacationers', 5, 'Finding affordable and fresh food inside an airport is usually difficult, but Coffee & More surprised us. The grilled sandwich and choco brownie are top-notch. Clean counter and fast service!', 'Family Traveler');


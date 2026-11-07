package com.coffeeandmore;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * High-performance Java HTTP & REST API Server for Coffee & More.
 * Connects directly to MySQL for live database storage and also serves the frontend website.
 */
public class CoffeeAndMoreServer {

    private static final int DEFAULT_PORT = 8080;
    private static DatabaseManager db;

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        try (InputStream in = new FileInputStream("backend/config.properties")) {
            Properties props = new Properties();
            props.load(in);
            port = Integer.parseInt(props.getProperty("server.port", String.valueOf(DEFAULT_PORT)));
        } catch (Exception ignored) {}

        db = DatabaseManager.getInstance();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        // REST API Contexts
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/menu", new MenuHandler());
        server.createContext("/api/contact", new ContactHandler());
        server.createContext("/api/reviews", new ReviewsHandler());
        server.createContext("/api/orders", new OrdersHandler());

        // Static Files Context (serves index.html, styles.css, script.js, coffee_animation.mp4)
        server.createContext("/", new StaticFileHandler());

        server.start();

        System.out.println("================================================================================");
        System.out.println("[SERVER] COFFEE & MORE - JAVA BACKEND SERVER RUNNING");
        System.out.println("================================================================================");
        System.out.println(">> Local Website URL : http://localhost:" + port + "/");
        System.out.println(">> REST API Status   : http://localhost:" + port + "/api/status");
        System.out.println(">> REST API Menu     : http://localhost:" + port + "/api/menu");
        System.out.println(">> REST API Reviews  : http://localhost:" + port + "/api/reviews");
        System.out.println(">> MySQL Connection  : " + (db.isConnected() ? "CONNECTED [Active]" : "FALLBACK CACHE [Ready for DB]"));
        System.out.println("================================================================================");
    }

    // =========================================================================
    // API HANDLERS
    // =========================================================================

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("status", "ONLINE");
            resp.put("brand", "Coffee & More");
            resp.put("location", "Kamaraj Domestic Airport, Chennai");
            resp.put("hours", "6:00 AM – 10:00 PM Daily");
            resp.put("isOpenNow", true);
            resp.put("mysqlConnected", db.isConnected());
            resp.put("timestamp", new Date().toString());

            sendJsonResponse(exchange, 200, toJson(resp));
        }
    }

    static class MenuHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            String category = queryParams.get("category");
            String search = queryParams.get("search");

            List<Map<String, Object>> items = db.getMenuItems(category, search);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("total", items.size());
            resp.put("source", db.isConnected() ? "MYSQL_DATABASE" : "MEMORY_FALLBACK");
            resp.put("items", items);

            sendJsonResponse(exchange, 200, toJson(resp));
        }
    }

    static class ContactHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = parseSimpleJson(body);

            String name = data.getOrDefault("fullName", data.getOrDefault("name", "Anonymous Traveler"));
            String mobile = data.getOrDefault("mobileNumber", data.getOrDefault("mobile", ""));
            String email = data.getOrDefault("emailAddress", data.getOrDefault("email", ""));
            String reason = data.getOrDefault("inquiryReason", data.getOrDefault("reason", "General"));
            String message = data.getOrDefault("messageText", data.getOrDefault("message", ""));

            boolean saved = db.saveContactInquiry(name, mobile, email, reason, message);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", saved);
            resp.put("message", "Thank you for reaching out! Our team at the airport counter will respond shortly.");
            resp.put("storedIn", db.isConnected() ? "MYSQL_DATABASE" : "LOCAL_MEMORY");
            resp.put("receivedAt", new Date().toString());

            sendJsonResponse(exchange, 201, toJson(resp));
        }
    }

    static class ReviewsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            List<Map<String, Object>> reviews = db.getTravelerReviews();
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("averageRating", 4.8);
            resp.put("totalReviews", reviews.size());
            resp.put("reviews", reviews);

            sendJsonResponse(exchange, 200, toJson(resp));
        }
    }

    static class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> data = parseSimpleJson(body);

            String name = data.getOrDefault("customerName", "Transit Passenger");
            String phone = data.getOrDefault("customerPhone", "");
            String flight = data.getOrDefault("flightNumber", "Not specified");
            String item = data.getOrDefault("itemName", "Signature Airport Cappuccino");
            int qty = Integer.parseInt(data.getOrDefault("quantity", "1"));
            String notes = data.getOrDefault("specialNotes", "Express counter pick-up");
            double total = Double.parseDouble(data.getOrDefault("totalPrice", "180.00"));

            boolean saved = db.savePassengerOrder(name, phone, flight, item, qty, notes, total);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", saved);
            resp.put("confirmation", "Order received! Your bite is being prepped fresh for airport counter pickup.");
            resp.put("pickupSpeed", "3 Minutes");

            sendJsonResponse(exchange, 201, toJson(resp));
        }
    }

    // =========================================================================
    // STATIC FILE HANDLER (Serves index.html, styles.css, script.js, video)
    // =========================================================================

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isBlank()) {
                path = "/index.html";
            }

            // Resolve file from workspace directory
            Path filePath = Paths.get("." + path).normalize();
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                filePath = Paths.get(path.substring(1)).normalize();
            }

            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                byte[] notFound = "<h1>404 — File Not Found</h1><p>Coffee & More Airport Café</p>".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFound.length);
                exchange.getResponseBody().write(notFound);
                exchange.getResponseBody().close();
                return;
            }

            String contentType = getMimeType(filePath.toString());
            exchange.getResponseHeaders().set("Content-Type", contentType);

            long fileLength = Files.size(filePath);

            // Handle video byte-range requests for smooth seeking
            String rangeHeader = exchange.getRequestHeaders().getFirst("Range");
            if (rangeHeader != null && rangeHeader.startsWith("bytes=") && contentType.startsWith("video/")) {
                handleRangeRequest(exchange, filePath, fileLength, rangeHeader);
                return;
            }

            exchange.sendResponseHeaders(200, fileLength);
            try (OutputStream os = exchange.getResponseBody(); InputStream is = Files.newInputStream(filePath)) {
                byte[] buffer = new byte[16384];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        }

        private void handleRangeRequest(HttpExchange exchange, Path filePath, long fileLength, String rangeHeader) throws IOException {
            String[] parts = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(parts[0]);
            long end = parts.length > 1 && !parts[1].isBlank() ? Long.parseLong(parts[1]) : fileLength - 1;
            if (end >= fileLength) end = fileLength - 1;
            long contentLength = end - start + 1;

            exchange.getResponseHeaders().set("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
            exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
            exchange.sendResponseHeaders(206, contentLength);

            try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r");
                 OutputStream os = exchange.getResponseBody()) {
                file.seek(start);
                byte[] buffer = new byte[16384];
                long remaining = contentLength;
                while (remaining > 0) {
                    int read = file.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                    if (read == -1) break;
                    os.write(buffer, 0, read);
                    remaining -= read;
                }
            }
        }

        private String getMimeType(String filename) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".mp4")) return "video/mp4";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            return "application/octet-stream";
        }
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Range, Authorization");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, val);
            }
        }
        return map;
    }

    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isBlank()) return map;
        String clean = json.trim();
        if (clean.startsWith("{")) clean = clean.substring(1);
        if (clean.endsWith("}")) clean = clean.substring(0, clean.length() - 1);

        String[] tokens = clean.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String token : tokens) {
            String[] pair = token.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
            if (pair.length == 2) {
                String k = pair[0].trim().replace("\"", "");
                String v = pair[1].trim().replace("\"", "");
                map.put(k, v);
            }
        }
        return map;
    }

    private static String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escapeJson((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            Map<?, ?> map = (Map<?, ?>) obj;
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (i++ > 0) sb.append(",");
                sb.append("\"").append(entry.getKey()).append("\":").append(toJson(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if (obj instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            List<?> list = (List<?>) obj;
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + obj.toString() + "\"";
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

# ☕ Coffee & More — Full-Stack Website & Java Backend

> **Kamaraj Domestic Airport, Chennai (GST Road, Meenambakkam)**  
> *Brews, Bites & Warm Delights*

A modern, responsive, animated website powered by a **Java 17 backend** and **MySQL 8.0 database**.

---

## 🏗️ Architecture Overview

```
coffee-shop-website/
├── index.html                 # Animated, responsive homepage UI (12 sections)
├── styles.css                 # Unified luxury roasted espresso design system
├── script.js                  # Dynamic video scroll-scrubbing & API integrations
├── coffee_animation.mp4       # Pre-existing coffee animation video
├── run-backend.bat            # 1-Click Windows batch launcher
├── run-backend.ps1            # PowerShell launcher
├── setup-mysql.sql            # Complete MySQL database schema & seed data
│
└── backend/
    ├── config.properties      # MySQL connection credentials & server port
    ├── lib/
    │   └── mysql-connector-j-8.3.0.jar  # Official MySQL JDBC Driver
    ├── src/com/coffeeandmore/
    │   ├── CoffeeAndMoreServer.java      # Java HTTP & REST API server
    │   └── DatabaseManager.java          # MySQL JDBC connection & data manager
    └── bin/                              # Compiled Java .class files
```

---

## 🗄️ MySQL Database Setup

1. Open **MySQL Command Line Client** or **MySQL Workbench**.
2. Run the provided SQL script:
   ```sql
   source setup-mysql.sql;
   ```
   *Or via terminal:*
   ```bash
   mysql -u root -p < setup-mysql.sql
   ```
3. This creates:
   - Database: `coffee_and_more_db`
   - Table `menu_items`: Populated with all 25 handcrafted items from `content.txt`
   - Table `contact_inquiries`: Stores customer feedback and flight crew bulk pre-orders
   - Table `passenger_orders`: Stores quick counter pre-orders with flight numbers
   - Table `traveler_reviews`: Pre-seeded with verified passenger reviews

4. Update your MySQL password in `backend/config.properties`:
   ```properties
   db.host=localhost
   db.port=3306
   db.name=coffee_and_more_db
   db.user=root
   db.password=YOUR_MYSQL_PASSWORD
   ```

---

## 🚀 How to Run the Java Backend Server

### Option A: Using the 1-Click Launcher
Double-click `run-backend.bat` (or right-click `run-backend.ps1` and choose *Run with PowerShell*).

### Option B: From Command Prompt / Terminal
```bash
# 1. Compile Java files
javac -encoding UTF-8 -cp "backend/lib/*" -d backend/bin backend/src/com/coffeeandmore/*.java

# 2. Run the Server
java -cp "backend/bin;backend/lib/*" com.coffeeandmore.CoffeeAndMoreServer
```

Once started:
- 🌐 **Live Website**: [http://localhost:8080/](http://localhost:8080/)
- 📊 **Server & DB Status**: [http://localhost:8080/api/status](http://localhost:8080/api/status)
- ☕ **Menu REST API**: [http://localhost:8080/api/menu](http://localhost:8080/api/menu)
- ⭐ **Reviews REST API**: [http://localhost:8080/api/reviews](http://localhost:8080/api/reviews)

---

## 🔌 REST API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/status` | Returns server health, operating hours, and MySQL connection status. |
| `GET` | `/api/menu` | Returns all menu items from MySQL (supports `?category=coffee` or `?search=muffin`). |
| `POST` | `/api/contact` | Receives contact messages and crew bulk orders, saving them into MySQL `contact_inquiries`. |
| `GET` | `/api/reviews` | Returns customer ratings and reviews from MySQL `traveler_reviews`. |
| `POST` | `/api/orders` | Pre-orders express flight bites with flight number for 3-minute counter pickup. |
| `GET` | `/*` | Streams static website files (`index.html`, `styles.css`, `script.js`, `coffee_animation.mp4`) with HTTP Range seeking support. |

---

## 🛡️ Graceful Auto-Fallback
If MySQL is not started or credentials differ during initial setup, the Java backend automatically uses its built-in memory fallback store. The server will **never crash** or throw 500 errors, allowing immediate development and testing!


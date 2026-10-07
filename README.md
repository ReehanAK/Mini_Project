# FoodieHub — Online Food Ordering System

An enterprise-grade **Online Food Ordering System** built using **Java Swing / AWT** and **JDBC** with **XAMPP MySQL / MariaDB (InnoDB Engine)**. 

The system implements a fully normalized **3NF & BCNF** relational schema and enforces **strict ACID transaction boundaries** to prevent invalid transactions, double charging, race conditions, and inventory overselling.

---

## 📋 Table of Contents
1. [Key Features](#key-features)
2. [Prerequisite: Database Normalization Justification](#prerequisite-database-normalization-justification)
3. [ACID Transaction Guarantees & Prevention of Invalid Transactions](#acid-transaction-guarantees--prevention-of-invalid-transactions)
4. [Relational Schema (7 Tables)](#relational-schema-7-tables)
5. [Project Architecture](#project-architecture)
6. [Prerequisites & System Requirements](#prerequisites--system-requirements)
7. [How to Run](#how-to-run)
8. [Automated Verification Suite](#automated-verification-suite)

---

## 🌟 Key Features

### 🍽️ 1. Restaurant & Menu Discovery
- **Live Search & Cuisine Filtering:** Browse restaurants by keywords (e.g., Italian, Indian, Japanese, Mexican, American) or ratings.
- **Categorized Menus:** Filter dishes by categories (Appetizers, Mains, Sides, Desserts, Beverages).
- **Inventory Awareness:** Live stock indicators (`In Stock (35)`, `Low Stock (Only 2 left!)`, `Sold Out`).
- **Single-Restaurant Order Integrity:** Prevents mixing items across multiple restaurants in a single delivery order.

### 🛒 2. Interactive Shopping Cart & Checkout
- **Real-Time Price Calculations:** Live breakdown of items subtotal, dynamic delivery fees (FREE on orders $35+), 5% taxes, and grand total.
- **Quantity Manipulator:** Live increment/decrement and item removal.
- **Customer Address & Notes:** Delivery address pre-filled from active customer profile with special cooking instructions.
- **Multi-Channel Payments:** Credit Card, Debit Card, UPI, Net Banking, and Cash on Delivery (COD).

### 📦 3. Live Order Tracking & Audit Trail
- **Visual Step Progress Bar:** Custom-painted GUI widget reflecting lifecycle stages: `Placed` → `Confirmed` → `Preparing` → `Out for Delivery` → `Delivered`.
- **Chronological Audit Trail:** Live table of status transitions sourced from `order_status_logs`.
- **Order Lifecycle Simulator:** Advance status button simulating kitchen and courier actions.
- **Atomic Order Cancellation & Refund:** Automatically restores reserved stock into `menu_items` and updates payment status to `REFUNDED`.
- **Printable Invoices:** Formatted receipt generator with itemized totals and transaction IDs.

### ⚙️ 4. Kitchen Dispatcher & Inventory Console
- **System-Wide Orders:** View and update status of orders across all customers.
- **Live Inventory Manager:** View item stocks, trigger restock (+25 units), or force out-of-stock to test constraint protection.

### 👥 5. Instant Multi-Customer Profile Switcher
- Switch between active customer accounts (`Alice Johnson`, `Bob Martinez`, `Charlie Davis`, `Diana Prince`) via the top header bar, or register a new customer on the fly.

### 📐 6. Embedded Normalization & Architecture Viewer
- Built-in documentation tab presenting normalization theory, dependency maps, and transaction integrity directly within the running desktop app.

---

## 📐 Prerequisite: Database Normalization Justification

### 1. Unnormalized Form (UNF)
A naive flat order record bundles multiple repeating items, customer contact details, restaurant addresses, payments, and timeline logs:
`Order(order_id, cust_name, cust_email, rest_name, item_names, item_prices, item_quantities, total, pay_method, status)`

* **Anomalies Identified:**
  - **Multi-Valued Repeating Groups:** Cannot represent variable items per order without arrays or comma-delimited strings.
  - **Insertion Anomaly:** A new restaurant or food item cannot exist in the database without someone placing an order. A new customer cannot sign up without buying food.
  - **Update Anomaly:** Updating a restaurant's phone number or address requires mutating every past historical order row.
  - **Deletion Anomaly:** Purging an order erases the customer or food item record entirely.

### 2. First Normal Form (1NF)
* **Rule:** All attribute domains must be atomic; every table must have a primary key; no repeating groups.
* **Resolution:** Food items split into individual records; scalar types assigned (`DECIMAL(10,2)` for currency, `INT` for quantities).

### 3. Second Normal Form (2NF)
* **Rule:** Must be in 1NF and eliminate all **Partial Functional Dependencies** on composite keys.
* **Resolution:** In an `OrderLineItem` composite relation with key `{order_id, item_id}`, `item_name`, `item_description`, and `standard_price` depend *only* on `item_id`, not on `order_id`.
  - Extracted **`menu_items`** (PK `item_id`).
  - Extracted **`order_items`** (PK `order_item_id`, FK `order_id`, FK `item_id`) storing the **snapshot unit price** at order time.

### 4. Third Normal Form (3NF)
* **Rule:** Must be in 2NF and eliminate all **Transitive Dependencies** ($X \to Y \to Z$).
* **Resolution:** In `orders`:
  - `order_id → customer_id → {customer_name, email, phone, address}`
  - `order_id → restaurant_id → {restaurant_name, cuisine, phone, address}`
  - `order_id → payment_id → {payment_method, payment_status, txn_reference}`
  - Decomposed into distinct relations: **`customers`**, **`restaurants`**, **`payments`**, and **`order_status_logs`**.

### 5. Boyce-Codd Normal Form (BCNF)
* **Rule:** For every non-trivial functional dependency $X \to Y$, $X$ must be a **Superkey**.
* **Result:** In all 7 tables, the only determinants are candidate keys (`customer_id`, `restaurant_id`, `item_id`, `order_id`, `payment_id`, `log_id`). The schema is strictly BCNF compliant.

---

## 🔒 ACID Transaction Guarantees & Prevention of Invalid Transactions

To satisfy the prerequisite that *"Database will maintain order information and prevent invalid transactions"*:

1. **Storage Engine: MySQL InnoDB**
   - Full ACID transaction compliance with row-level locks and write-ahead redo logs.
2. **Pessimistic Inventory Locking:**
   ```sql
   SELECT stock_quantity, is_available FROM menu_items WHERE item_id = ? FOR UPDATE;
   ```
3. **Atomic Decrement:**
   ```sql
   UPDATE menu_items SET stock_quantity = stock_quantity - ? WHERE item_id = ? AND stock_quantity >= ?;
   ```
   If rows updated is 0, the system immediately throws an exception and issues `conn.rollback()`.
4. **Constraint Enforcement:**
   - `CHECK (stock_quantity >= 0)`
   - `CHECK (quantity > 0)`
   - `CHECK (total_amount >= 0.00)`
   - `UNIQUE (order_id)` on `payments` table (prevents double charging).
   - `ON DELETE RESTRICT` on customers and restaurants (prevents deleting active entities with dependent orders).
5. **Atomic Rollback on Failure:**
   All 6 steps (stock check, stock deduction, order master creation, line items batch insertion, payment insertion, status logging) execute within `conn.setAutoCommit(false)` and `conn.commit()`. Any network or constraint failure rolls back 100% of mutations.

---

## 🗄️ Relational Schema (7 Tables)

```
customers (customer_id PK, full_name, email UNIQUE, phone, address, created_at)
restaurants (restaurant_id PK, name, cuisine_type, address, phone, rating, is_active, created_at)
menu_items (item_id PK, restaurant_id FK, name, description, category, price, is_available, stock_quantity)
orders (order_id PK, customer_id FK, restaurant_id FK, order_date, total_amount, status, delivery_address, special_instructions)
order_items (order_item_id PK, order_id FK, item_id FK, quantity, unit_price, subtotal)
payments (payment_id PK, order_id FK UNIQUE, payment_date, amount, payment_method, payment_status, transaction_reference)
order_status_logs (log_id PK, order_id FK, status, remarks, updated_at)
```

---

## 📂 Project Architecture

```
order-java/
├── lib/
│   ├── mysql-connector-j-8.3.0.jar     # MySQL JDBC Driver
│   └── flatlaf-3.4.jar                 # Modern Swing Look and Feel
├── sql/
│   └── schema.sql                      # Complete DDL script with constraints & seed data
├── src/com/foodsystem/
│   ├── config/
│   │   ├── DatabaseConfig.java         # Connection settings
│   │   └── DBConnection.java           # JDBC connection provider & schema auto-initialization
│   ├── models/
│   │   ├── Customer.java
│   │   ├── Restaurant.java
│   │   ├── MenuItem.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Payment.java
│   │   ├── OrderStatusLog.java
│   │   ├── CartItem.java
│   │   └── enums/
│   │       ├── OrderStatus.java
│   │       ├── PaymentMethod.java
│   │       └── PaymentStatus.java
│   ├── dao/
│   │   ├── CustomerDAO.java
│   │   ├── RestaurantDAO.java
│   │   ├── MenuItemDAO.java
│   │   ├── OrderDAO.java               # Atomic transactions with rollback
│   │   ├── PaymentDAO.java
│   │   └── OrderStatusLogDAO.java
│   ├── service/
│   │   ├── CartManager.java            # Client-side cart manager
│   │   └── OrderService.java           # Facade orchestrating DAOs
│   ├── ui/
│   │   ├── MainFrame.java              # Top navigation, customer switcher & tabbed container
│   │   ├── components/
│   │   │   ├── UITheme.java            # Curated modern color palette & fonts
│   │   │   ├── CardPanel.java          # Card container with subtle border
│   │   │   └── OrderStepProgressBar.java # Custom graphics delivery stage progress bar
│   │   └── panels/
│   │       ├── BrowseRestaurantsPanel.java
│   │       ├── CartCheckoutPanel.java
│   │       ├── OrderTrackingPanel.java
│   │       ├── AdminManagementPanel.java
│   │       └── NormalizationJustificationPanel.java
│   ├── test/
│   │   └── SystemVerificationTest.java # Automated verification suite
│   └── Main.java                       # Application bootstrap
├── run.bat                             # Windows launcher script
├── test.bat                            # Test suite runner script
└── README.md                           # Documentation
```

---

## ⚙️ Prerequisites & System Requirements
- **Java Development Kit (JDK):** JDK 17 or newer (`javac` and `java` in PATH).
- **Database:** XAMPP MySQL / MariaDB running on port 3306.

---

## 🚀 How to Run

### Method 1: Using the Batch File (Recommended)
Simply double click or run:
```cmd
run.bat
```

### Method 2: Manual Terminal Execution
```cmd
REM 1. Compile
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt

REM 2. Run
java -cp "bin;lib/*" com.foodsystem.Main
```

---

## 🧪 Automated Verification Suite
To run the automated tests validating JDBC transactions, rollback, inventory refund, and 3NF schema:
```cmd
test.bat
```
*(Tests output 5/5 PASSED verifying ACID guarantees and inventory protection)*.

-- =====================================================================
-- ONLINE FOOD ORDERING SYSTEM DATABASE SCHEMA
-- Engine: MySQL / MariaDB (InnoDB for full ACID transaction support)
-- Normalization Level: 3NF & BCNF Compliant
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `food_ordering_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `food_ordering_db`;

-- Drop existing tables in reverse dependency order to avoid foreign key errors
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `order_status_logs`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `menu_items`;
DROP TABLE IF EXISTS `restaurants`;
DROP TABLE IF EXISTS `customers`;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- Table: customers
-- Purpose: Stores registered customer details. Separated from orders
-- to eliminate update anomalies and transitive dependencies (3NF).
-- ---------------------------------------------------------------------
CREATE TABLE `customers` (
    `customer_id` INT AUTO_INCREMENT PRIMARY KEY,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NOT NULL,
    `address` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_cust_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: restaurants
-- Purpose: Stores restaurant profiles, cuisines, and contact information.
-- Eliminates redundant restaurant details across order records.
-- ---------------------------------------------------------------------
CREATE TABLE `restaurants` (
    `restaurant_id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `cuisine_type` VARCHAR(50) NOT NULL,
    `address` VARCHAR(200) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `rating` DECIMAL(2,1) DEFAULT 4.5 CHECK (`rating` >= 1.0 AND `rating` <= 5.0),
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_rest_cuisine` (`cuisine_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: menu_items
-- Purpose: Stores food items linked to a specific restaurant.
-- 2NF separation ensures item details depend solely on item_id, not on (order_id, item_id).
-- Includes inventory stock tracking to prevent invalid oversold orders.
-- ---------------------------------------------------------------------
CREATE TABLE `menu_items` (
    `item_id` INT AUTO_INCREMENT PRIMARY KEY,
    `restaurant_id` INT NOT NULL,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT,
    `category` VARCHAR(50) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL CHECK (`price` >= 0.00),
    `is_available` BOOLEAN DEFAULT TRUE,
    `stock_quantity` INT NOT NULL DEFAULT 50 CHECK (`stock_quantity` >= 0),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`restaurant_id`) REFERENCES `restaurants`(`restaurant_id`) ON DELETE CASCADE,
    INDEX `idx_menu_restaurant` (`restaurant_id`),
    INDEX `idx_menu_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: orders
-- Purpose: Master order record capturing customer, restaurant, timestamp,
-- status lifecycle, and delivery destination.
-- ---------------------------------------------------------------------
CREATE TABLE `orders` (
    `order_id` INT AUTO_INCREMENT PRIMARY KEY,
    `customer_id` INT NOT NULL,
    `restaurant_id` INT NOT NULL,
    `order_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `total_amount` DECIMAL(10,2) NOT NULL CHECK (`total_amount` >= 0.00),
    `status` ENUM('PENDING', 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    `delivery_address` TEXT NOT NULL,
    `special_instructions` TEXT,
    FOREIGN KEY (`customer_id`) REFERENCES `customers`(`customer_id`) ON DELETE RESTRICT,
    FOREIGN KEY (`restaurant_id`) REFERENCES `restaurants`(`restaurant_id`) ON DELETE RESTRICT,
    INDEX `idx_orders_customer` (`customer_id`),
    INDEX `idx_orders_restaurant` (`restaurant_id`),
    INDEX `idx_orders_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: order_items
-- Purpose: Line items for orders. Resolves M:N relationship between
-- orders and menu_items (1NF/2NF decomposition).
-- Snapshots unit_price at time of purchase to insulate past orders
-- from future menu price alterations.
-- ---------------------------------------------------------------------
CREATE TABLE `order_items` (
    `order_item_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,
    `item_id` INT NOT NULL,
    `quantity` INT NOT NULL CHECK (`quantity` > 0),
    `unit_price` DECIMAL(10,2) NOT NULL CHECK (`unit_price` >= 0.00),
    `subtotal` DECIMAL(10,2) NOT NULL CHECK (`subtotal` >= 0.00),
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    FOREIGN KEY (`item_id`) REFERENCES `menu_items`(`item_id`) ON DELETE RESTRICT,
    INDEX `idx_order_items_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: payments
-- Purpose: Records financial transaction corresponding to an order.
-- 1-to-1 relationship enforced via UNIQUE constraint on order_id.
-- Prevents duplicate payment submissions and tracks payment status.
-- ---------------------------------------------------------------------
CREATE TABLE `payments` (
    `payment_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL UNIQUE,
    `payment_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `amount` DECIMAL(10,2) NOT NULL CHECK (`amount` >= 0.00),
    `payment_method` ENUM('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'CASH_ON_DELIVERY', 'NET_BANKING') NOT NULL,
    `payment_status` ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    `transaction_reference` VARCHAR(64) NOT NULL,
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    INDEX `idx_payments_order` (`order_id`),
    INDEX `idx_payments_ref` (`transaction_reference`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- Table: order_status_logs
-- Purpose: Audit log recording every state transition of an order.
-- Ensures full observability and auditability of order lifecycle.
-- ---------------------------------------------------------------------
CREATE TABLE `order_status_logs` (
    `log_id` INT AUTO_INCREMENT PRIMARY KEY,
    `order_id` INT NOT NULL,
    `status` VARCHAR(30) NOT NULL,
    `remarks` TEXT,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE,
    INDEX `idx_logs_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================================
-- SEED DATA INSERTION
-- Realistic sample records for demonstration and testing
-- =====================================================================

-- Customers
INSERT INTO `customers` (`customer_id`, `full_name`, `email`, `phone`, `address`) VALUES
(1, 'Alice Johnson', 'alice.johnson@example.com', '+1-555-0101', '742 Evergreen Terrace, Springfield'),
(2, 'Bob Martinez', 'bob.martinez@example.com', '+1-555-0102', '123 Elm Street, Metropolis'),
(3, 'Charlie Davis', 'charlie.davis@example.com', '+1-555-0103', '456 Oak Avenue, Gotham City'),
(4, 'Diana Prince', 'diana.prince@example.com', '+1-555-0104', '100 Gateway Boulevard, Star City');

-- Restaurants
INSERT INTO `restaurants` (`restaurant_id`, `name`, `cuisine_type`, `address`, `phone`, `rating`, `is_active`) VALUES
(1, 'Bella Italia Trattoria', 'Italian', '14 Roma Way, Springfield', '+1-555-1101', 4.8, TRUE),
(2, 'Royal Spice Palace', 'Indian', '88 Curry Crescent, Springfield', '+1-555-1102', 4.9, TRUE),
(3, 'Sakura Sushi Bar', 'Japanese', '210 Lotus Court, Metropolis', '+1-555-1103', 4.7, TRUE),
(4, 'El Sombrero Taqueria', 'Mexican', '45 Sunset Strip, Metropolis', '+1-555-1104', 4.6, TRUE),
(5, 'Craft Burger Co.', 'American', '90 Downtown Mall, Gotham City', '+1-555-1105', 4.5, TRUE);

-- Menu Items
INSERT INTO `menu_items` (`item_id`, `restaurant_id`, `name`, `description`, `category`, `price`, `is_available`, `stock_quantity`) VALUES
-- Bella Italia
(1, 1, 'Margherita Pizza D.O.P.', 'San Marzano tomatoes, fresh buffalo mozzarella, fresh basil, extra virgin olive oil', 'Mains', 14.50, TRUE, 40),
(2, 1, 'Truffle Tagliatelle', 'Handmade egg pasta with black truffle cream sauce and shaved parmesan', 'Mains', 18.00, TRUE, 30),
(3, 1, 'Classic Bruschetta', 'Toasted rustic bread topped with garlic, vine-ripened tomatoes, and basil', 'Appetizers', 8.50, TRUE, 50),
(4, 1, 'Tiramisu Tradizionale', 'Espresso-soaked ladyfingers with rich mascarpone cream and cocoa powder', 'Desserts', 7.50, TRUE, 25),
(5, 1, 'San Pellegrino Sparkling Water', 'Natural Italian sparkling mineral water (750ml)', 'Beverages', 4.00, TRUE, 60),

-- Royal Spice Palace
(6, 2, 'Butter Chicken (Murgh Makhani)', 'Tender roasted chicken simmered in a creamy tomato and fenugreek gravy', 'Mains', 16.00, TRUE, 45),
(7, 2, 'Paneer Tikka Masala', 'Charcoal grilled cottage cheese cubes in aromatic spiced onion-tomato masala', 'Mains', 14.00, TRUE, 35),
(8, 2, 'Garlic Butter Naan', 'Tandoor-baked flatbread glazed with melted garlic butter and cilantro', 'Sides', 3.50, TRUE, 80),
(9, 2, 'Vegetable Samosas (3 pcs)', 'Crispy hand-folded pastry stuffed with spiced potatoes, peas, and mint chutney', 'Appetizers', 6.00, TRUE, 50),
(10, 2, 'Mango Lassi', 'Chilled yogurt smoothie infused with sweet Alphonso mango pulp and cardamom', 'Beverages', 4.50, TRUE, 40),

-- Sakura Sushi Bar
(11, 3, 'Dragon Roll (8 pcs)', 'Shrimp tempura, avocado, cucumber topped with eel, avocado, and unagi glaze', 'Sushi Rolls', 17.50, TRUE, 30),
(12, 3, 'Salmon & Tuna Nigiri Combo', 'Fresh Norwegian salmon and bluefin tuna nigiri with wasabi and pickled ginger', 'Sushi', 19.00, TRUE, 25),
(13, 3, 'Tonkotsu Ramen', 'Rich 12-hour pork bone broth, tender chashu pork, bamboo shoots, and soft egg', 'Mains', 15.50, TRUE, 35),
(14, 3, 'Steamed Edamame', 'Young soybeans steamed and tossed with flaky Maldon sea salt', 'Appetizers', 5.50, TRUE, 50),
(15, 3, 'Matcha Green Tea Mochi', 'Japanese sweet rice dough filled with premium green tea ice cream', 'Desserts', 6.50, TRUE, 30),

-- El Sombrero Taqueria
(16, 4, 'Carne Asada Tacos (3 pcs)', 'Marinated grilled steak, chopped white onions, cilantro, salsa verde on corn tortillas', 'Tacos', 13.50, TRUE, 40),
(17, 4, 'Baja Fish Tacos', 'Beer-battered cod, chipotle crema, shredded red cabbage, pico de gallo', 'Tacos', 14.00, TRUE, 35),
(18, 4, 'Loaded Guacamole & Chips', 'Freshly smashed avocados, lime, jalapeño, pico de gallo with house tortilla chips', 'Appetizers', 9.00, TRUE, 45),
(19, 4, 'Cinnamon Sugar Churros', 'Warm golden churros rolled in Mexican cinnamon sugar served with warm dulce de leche', 'Desserts', 6.50, TRUE, 30),
(20, 4, 'Horchata Fresca', 'Traditional sweet rice milk drink with Mexican cinnamon and vanilla', 'Beverages', 4.00, TRUE, 50),

-- Craft Burger Co.
(21, 5, 'The Double Smash Burger', 'Two prime beef patties, American cheese, grilled onions, house secret sauce, brioche bun', 'Burgers', 13.00, TRUE, 50),
(22, 5, 'Crispy Hot Honey Chicken', 'Buttermilk fried chicken breast, spicy honey glaze, dill pickles, creamy slaw', 'Burgers', 12.50, TRUE, 40),
(23, 5, 'Truffle Parmesan Fries', 'Hand-cut russet potato fries tossed in white truffle oil, grated parmesan, parsley', 'Sides', 6.50, TRUE, 60),
(24, 5, 'Chocolate Fudge Milkshake', 'Thick hand-spun vanilla bean ice cream blended with dark Dutch chocolate fudge', 'Beverages', 5.50, TRUE, 40);

-- Sample Orders
INSERT INTO `orders` (`order_id`, `customer_id`, `restaurant_id`, `order_date`, `total_amount`, `status`, `delivery_address`, `special_instructions`) VALUES
(101, 1, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), 40.50, 'DELIVERED', '742 Evergreen Terrace, Springfield', 'Please ring the doorbell and leave at door'),
(102, 2, 2, DATE_SUB(NOW(), INTERVAL 45 MINUTE), 28.00, 'PREPARING', '123 Elm Street, Metropolis', 'Extra mint chutney please'),
(103, 1, 3, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 38.50, 'CONFIRMED', '742 Evergreen Terrace, Springfield', 'Include extra chopsticks and soy sauce');

-- Order Items
INSERT INTO `order_items` (`order_item_id`, `order_id`, `item_id`, `quantity`, `unit_price`, `subtotal`) VALUES
-- Order 101: Bella Italia
(1, 101, 1, 2, 14.50, 29.00),
(2, 101, 4, 1, 7.50, 7.50),
(3, 101, 5, 1, 4.00, 4.00),

-- Order 102: Royal Spice Palace
(4, 102, 6, 1, 16.00, 16.00),
(5, 102, 8, 2, 3.50, 7.00),
(6, 102, 10, 1, 4.50, 4.50),

-- Order 103: Sakura Sushi
(7, 103, 11, 1, 17.50, 17.50),
(8, 103, 13, 1, 15.50, 15.50),
(9, 103, 15, 1, 6.50, 6.50);

-- Payments
INSERT INTO `payments` (`payment_id`, `order_id`, `payment_date`, `amount`, `payment_method`, `payment_status`, `transaction_reference`) VALUES
(1, 101, DATE_SUB(NOW(), INTERVAL 2 HOUR), 40.50, 'CREDIT_CARD', 'SUCCESS', 'TXN-CARD-98432101'),
(2, 102, DATE_SUB(NOW(), INTERVAL 45 MINUTE), 28.00, 'UPI', 'SUCCESS', 'TXN-UPI-88421092'),
(3, 103, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 38.50, 'NET_BANKING', 'SUCCESS', 'TXN-NET-77192034');

-- Order Status Logs
INSERT INTO `order_status_logs` (`order_id`, `status`, `remarks`, `updated_at`) VALUES
(101, 'PENDING', 'Order submitted by customer', DATE_SUB(NOW(), INTERVAL 120 MINUTE)),
(101, 'CONFIRMED', 'Restaurant accepted order', DATE_SUB(NOW(), INTERVAL 115 MINUTE)),
(101, 'PREPARING', 'Chef is baking pizza', DATE_SUB(NOW(), INTERVAL 105 MINUTE)),
(101, 'OUT_FOR_DELIVERY', 'Delivery partner dispatched', DATE_SUB(NOW(), INTERVAL 75 MINUTE)),
(101, 'DELIVERED', 'Package safely delivered at front porch', DATE_SUB(NOW(), INTERVAL 50 MINUTE)),

(102, 'PENDING', 'Order submitted by customer', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
(102, 'CONFIRMED', 'Restaurant accepted order', DATE_SUB(NOW(), INTERVAL 40 MINUTE)),
(102, 'PREPARING', 'Curries simmering in kitchen', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),

(103, 'PENDING', 'Order submitted by customer', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(103, 'CONFIRMED', 'Sushi master confirmed ticket', DATE_SUB(NOW(), INTERVAL 10 MINUTE));

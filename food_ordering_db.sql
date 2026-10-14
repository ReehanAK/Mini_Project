-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 14, 2026 at 02:11 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `food_ordering_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `address` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `full_name`, `email`, `phone`, `address`, `created_at`) VALUES
(1, 'Reehan', 'reehan67612@gmail.com', '9012230008', '3, Raja Street', '2026-10-05 18:04:44'),
(6, 'prem', 'PREMDARKBOY@GMAIL.COM', '892323232320', 'RED FORT', '2026-10-07 08:41:16');

-- --------------------------------------------------------

--
-- Table structure for table `menu_items`
--

CREATE TABLE `menu_items` (
  `item_id` int(11) NOT NULL,
  `restaurant_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `category` varchar(50) NOT NULL,
  `price` decimal(10,2) NOT NULL CHECK (`price` >= 0.00),
  `is_available` tinyint(1) DEFAULT 1,
  `stock_quantity` int(11) NOT NULL DEFAULT 50 CHECK (`stock_quantity` >= 0),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `menu_items`
--

INSERT INTO `menu_items` (`item_id`, `restaurant_id`, `name`, `description`, `category`, `price`, `is_available`, `stock_quantity`, `created_at`) VALUES
(1, 1, 'Margherita Pizza D.O.P.', 'San Marzano tomatoes, fresh buffalo mozzarella, fresh basil, extra virgin olive oil', 'Mains', 14.50, 1, 40, '2026-10-05 17:37:21'),
(2, 1, 'Truffle Tagliatelle', 'Handmade egg pasta with black truffle cream sauce and shaved parmesan', 'Mains', 18.00, 1, 30, '2026-10-05 17:37:21'),
(3, 1, 'Classic Bruschetta', 'Toasted rustic bread topped with garlic, vine-ripened tomatoes, and basil', 'Appetizers', 8.50, 1, 50, '2026-10-05 17:37:21'),
(4, 1, 'Tiramisu Tradizionale', 'Espresso-soaked ladyfingers with rich mascarpone cream and cocoa powder', 'Desserts', 7.50, 1, 25, '2026-10-05 17:37:21'),
(5, 1, 'San Pellegrino Sparkling Water', 'Natural Italian sparkling mineral water (750ml)', 'Beverages', 4.00, 1, 60, '2026-10-05 17:37:21'),
(6, 2, 'Butter Chicken (Murgh Makhani)', 'Tender roasted chicken simmered in a creamy tomato and fenugreek gravy', 'Mains', 16.00, 1, 45, '2026-10-05 17:37:21'),
(7, 2, 'Paneer Tikka Masala', 'Charcoal grilled cottage cheese cubes in aromatic spiced onion-tomato masala', 'Mains', 14.00, 1, 35, '2026-10-05 17:37:21'),
(8, 2, 'Garlic Butter Naan', 'Tandoor-baked flatbread glazed with melted garlic butter and cilantro', 'Sides', 3.50, 1, 80, '2026-10-05 17:37:21'),
(9, 2, 'Vegetable Samosas (3 pcs)', 'Crispy hand-folded pastry stuffed with spiced potatoes, peas, and mint chutney', 'Appetizers', 6.00, 1, 38, '2026-10-05 17:37:21'),
(10, 2, 'Mango Lassi', 'Chilled yogurt smoothie infused with sweet Alphonso mango pulp and cardamom', 'Beverages', 4.50, 1, 36, '2026-10-05 17:37:21'),
(11, 3, 'Dragon Roll (8 pcs)', 'Shrimp tempura, avocado, cucumber topped with eel, avocado, and unagi glaze', 'Sushi Rolls', 17.50, 1, 30, '2026-10-05 17:37:21'),
(12, 3, 'Salmon & Tuna Nigiri Combo', 'Fresh Norwegian salmon and bluefin tuna nigiri with wasabi and pickled ginger', 'Sushi', 19.00, 1, 25, '2026-10-05 17:37:21'),
(13, 3, 'Tonkotsu Ramen', 'Rich 12-hour pork bone broth, tender chashu pork, bamboo shoots, and soft egg', 'Mains', 15.50, 1, 35, '2026-10-05 17:37:21'),
(14, 3, 'Steamed Edamame', 'Young soybeans steamed and tossed with flaky Maldon sea salt', 'Appetizers', 5.50, 1, 50, '2026-10-05 17:37:21'),
(15, 3, 'Matcha Green Tea Mochi', 'Japanese sweet rice dough filled with premium green tea ice cream', 'Desserts', 6.50, 1, 30, '2026-10-05 17:37:21'),
(16, 4, 'Carne Asada Tacos (3 pcs)', 'Marinated grilled steak, chopped white onions, cilantro, salsa verde on corn tortillas', 'Tacos', 13.50, 1, 40, '2026-10-05 17:37:21'),
(17, 4, 'Baja Fish Tacos', 'Beer-battered cod, chipotle crema, shredded red cabbage, pico de gallo', 'Tacos', 14.00, 1, 35, '2026-10-05 17:37:21'),
(18, 4, 'Loaded Guacamole & Chips', 'Freshly smashed avocados, lime, jalape├▒o, pico de gallo with house tortilla chips', 'Appetizers', 9.00, 1, 45, '2026-10-05 17:37:21'),
(19, 4, 'Cinnamon Sugar Churros', 'Warm golden churros rolled in Mexican cinnamon sugar served with warm dulce de leche', 'Desserts', 6.50, 1, 30, '2026-10-05 17:37:21'),
(20, 4, 'Horchata Fresca', 'Traditional sweet rice milk drink with Mexican cinnamon and vanilla', 'Beverages', 4.00, 1, 50, '2026-10-05 17:37:21'),
(21, 5, 'The Double Smash Burger', 'Two prime beef patties, American cheese, grilled onions, house secret sauce, brioche bun', 'Burgers', 13.00, 1, 50, '2026-10-05 17:37:21'),
(22, 5, 'Crispy Hot Honey Chicken', 'Buttermilk fried chicken breast, spicy honey glaze, dill pickles, creamy slaw', 'Burgers', 12.50, 1, 40, '2026-10-05 17:37:21'),
(23, 5, 'Truffle Parmesan Fries', 'Hand-cut russet potato fries tossed in white truffle oil, grated parmesan, parsley', 'Sides', 6.50, 1, 60, '2026-10-05 17:37:21'),
(24, 5, 'Chocolate Fudge Milkshake', 'Thick hand-spun vanilla bean ice cream blended with dark Dutch chocolate fudge', 'Beverages', 5.50, 1, 40, '2026-10-05 17:37:21');

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `customer_id` int(11) NOT NULL,
  `restaurant_id` int(11) NOT NULL,
  `order_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_amount` decimal(10,2) NOT NULL CHECK (`total_amount` >= 0.00),
  `status` enum('PENDING','CONFIRMED','PREPARING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED') DEFAULT 'PENDING',
  `delivery_address` text NOT NULL,
  `special_instructions` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`order_id`, `customer_id`, `restaurant_id`, `order_date`, `total_amount`, `status`, `delivery_address`, `special_instructions`) VALUES
(106, 1, 2, '2026-10-05 18:07:14', 44.10, 'PENDING', '3, Raja Street', ''),
(107, 1, 2, '2026-10-07 08:17:37', 15.02, 'DELIVERED', '3, Raja Street', ''),
(108, 6, 2, '2026-10-07 08:42:07', 80.33, 'CANCELLED', 'RED FORT', ''),
(109, 1, 2, '2026-10-14 11:41:07', 30.77, 'DELIVERED', 'RED FORT', '');

-- --------------------------------------------------------

--
-- Table structure for table `order_items`
--

CREATE TABLE `order_items` (
  `order_item_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `item_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL CHECK (`quantity` > 0),
  `unit_price` decimal(10,2) NOT NULL CHECK (`unit_price` >= 0.00),
  `subtotal` decimal(10,2) NOT NULL CHECK (`subtotal` >= 0.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_items`
--

INSERT INTO `order_items` (`order_item_id`, `order_id`, `item_id`, `quantity`, `unit_price`, `subtotal`) VALUES
(1, 106, 9, 7, 6.00, 42.00),
(2, 107, 9, 1, 6.00, 6.00),
(3, 107, 10, 1, 4.50, 4.50),
(4, 108, 9, 7, 6.00, 42.00),
(5, 108, 6, 1, 16.00, 16.00),
(6, 108, 10, 1, 4.50, 4.50),
(7, 108, 7, 1, 14.00, 14.00),
(8, 109, 9, 2, 6.00, 12.00),
(9, 109, 10, 3, 4.50, 13.50);

-- --------------------------------------------------------

--
-- Table structure for table `order_status_logs`
--

CREATE TABLE `order_status_logs` (
  `log_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `status` varchar(30) NOT NULL,
  `remarks` text DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `order_status_logs`
--

INSERT INTO `order_status_logs` (`log_id`, `order_id`, `status`, `remarks`, `updated_at`) VALUES
(1, 106, 'PENDING', 'Order submitted and payment authorized (TXN-CASH-AA1821DB)', '2026-10-05 18:07:14'),
(2, 107, 'PENDING', 'Order submitted and payment authorized (TXN-CASH-89C6ED2B)', '2026-10-07 08:17:37'),
(3, 107, 'CONFIRMED', 'j', '2026-10-07 08:17:54'),
(4, 107, 'PREPARING', 'confirm', '2026-10-07 08:18:01'),
(5, 107, 'OUT_FOR_DELIVERY', 'confirm', '2026-10-07 08:18:09'),
(6, 107, 'DELIVERED', 'confirm', '2026-10-07 08:18:18'),
(7, 108, 'PENDING', 'Order submitted and payment authorized (TXN-CRED-D16A94AE)', '2026-10-07 08:42:07'),
(8, 108, 'CONFIRMED', 'R', '2026-10-07 08:42:21'),
(9, 108, 'PREPARING', 'EW', '2026-10-07 08:42:25'),
(10, 108, 'CANCELLED', 'Order cancelled: Customer changed mind (Inventory refunded)', '2026-10-07 08:47:45'),
(11, 109, 'PENDING', 'Order submitted and payment authorized (TXN-CASH-CF3CCC7A)', '2026-10-14 11:41:07'),
(12, 109, 'CONFIRMED', 'confirm', '2026-10-14 11:41:30'),
(13, 109, 'PREPARING', 'preparing', '2026-10-14 11:41:40'),
(14, 109, 'OUT_FOR_DELIVERY', 'out of delivery', '2026-10-14 11:41:54'),
(15, 109, 'DELIVERED', 'deliveired', '2026-10-14 11:42:03');

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `payment_id` int(11) NOT NULL,
  `order_id` int(11) NOT NULL,
  `payment_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `amount` decimal(10,2) NOT NULL CHECK (`amount` >= 0.00),
  `payment_method` enum('CREDIT_CARD','DEBIT_CARD','UPI','CASH_ON_DELIVERY','NET_BANKING') NOT NULL,
  `payment_status` enum('PENDING','SUCCESS','FAILED','REFUNDED') DEFAULT 'PENDING',
  `transaction_reference` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`payment_id`, `order_id`, `payment_date`, `amount`, `payment_method`, `payment_status`, `transaction_reference`) VALUES
(6, 106, '2026-10-05 18:07:14', 44.10, 'CASH_ON_DELIVERY', 'SUCCESS', 'TXN-CASH-AA1821DB'),
(7, 107, '2026-10-07 08:17:37', 15.02, 'CASH_ON_DELIVERY', 'SUCCESS', 'TXN-CASH-89C6ED2B'),
(8, 108, '2026-10-07 08:42:07', 80.33, 'CREDIT_CARD', 'REFUNDED', 'TXN-CRED-D16A94AE'),
(9, 109, '2026-10-14 11:41:07', 30.77, 'CASH_ON_DELIVERY', 'SUCCESS', 'TXN-CASH-CF3CCC7A');

-- --------------------------------------------------------

--
-- Table structure for table `restaurants`
--

CREATE TABLE `restaurants` (
  `restaurant_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `cuisine_type` varchar(50) NOT NULL,
  `address` varchar(200) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `rating` decimal(2,1) DEFAULT 4.5 CHECK (`rating` >= 1.0 and `rating` <= 5.0),
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `restaurants`
--

INSERT INTO `restaurants` (`restaurant_id`, `name`, `cuisine_type`, `address`, `phone`, `rating`, `is_active`, `created_at`) VALUES
(1, 'Bella Italia Trattoria', 'Italian', '14 Roma Way, Springfield', '+1-555-1101', 4.8, 1, '2026-10-05 17:37:21'),
(2, 'Royal Spice Palace', 'Indian', '88 Curry Crescent, Springfield', '+1-555-1102', 4.9, 1, '2026-10-05 17:37:21'),
(3, 'Sakura Sushi Bar', 'Japanese', '210 Lotus Court, Metropolis', '+1-555-1103', 4.7, 1, '2026-10-05 17:37:21'),
(4, 'El Sombrero Taqueria', 'Mexican', '45 Sunset Strip, Metropolis', '+1-555-1104', 4.6, 1, '2026-10-05 17:37:21'),
(5, 'Craft Burger Co.', 'American', '90 Downtown Mall, Gotham City', '+1-555-1105', 4.5, 1, '2026-10-05 17:37:21');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_cust_email` (`email`);

--
-- Indexes for table `menu_items`
--
ALTER TABLE `menu_items`
  ADD PRIMARY KEY (`item_id`),
  ADD KEY `idx_menu_restaurant` (`restaurant_id`),
  ADD KEY `idx_menu_category` (`category`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `idx_orders_customer` (`customer_id`),
  ADD KEY `idx_orders_restaurant` (`restaurant_id`),
  ADD KEY `idx_orders_status` (`status`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`order_item_id`),
  ADD KEY `item_id` (`item_id`),
  ADD KEY `idx_order_items_order` (`order_id`);

--
-- Indexes for table `order_status_logs`
--
ALTER TABLE `order_status_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `idx_logs_order` (`order_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`payment_id`),
  ADD UNIQUE KEY `order_id` (`order_id`),
  ADD KEY `idx_payments_order` (`order_id`),
  ADD KEY `idx_payments_ref` (`transaction_reference`);

--
-- Indexes for table `restaurants`
--
ALTER TABLE `restaurants`
  ADD PRIMARY KEY (`restaurant_id`),
  ADD KEY `idx_rest_cuisine` (`cuisine_type`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `menu_items`
--
ALTER TABLE `menu_items`
  MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=110;

--
-- AUTO_INCREMENT for table `order_items`
--
ALTER TABLE `order_items`
  MODIFY `order_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `order_status_logs`
--
ALTER TABLE `order_status_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `payment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `restaurants`
--
ALTER TABLE `restaurants`
  MODIFY `restaurant_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `menu_items`
--
ALTER TABLE `menu_items`
  ADD CONSTRAINT `menu_items_ibfk_1` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurants` (`restaurant_id`) ON DELETE CASCADE;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`),
  ADD CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurants` (`restaurant_id`);

--
-- Constraints for table `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`item_id`) REFERENCES `menu_items` (`item_id`);

--
-- Constraints for table `order_status_logs`
--
ALTER TABLE `order_status_logs`
  ADD CONSTRAINT `order_status_logs_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE;

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

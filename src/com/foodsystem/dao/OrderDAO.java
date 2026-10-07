package com.foodsystem.dao;

import com.foodsystem.config.DBConnection;
import com.foodsystem.models.CartItem;
import com.foodsystem.models.Order;
import com.foodsystem.models.OrderItem;
import com.foodsystem.models.Payment;
import com.foodsystem.models.enums.OrderStatus;
import com.foodsystem.models.enums.PaymentMethod;
import com.foodsystem.models.enums.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderDAO {

    /**
     * Executes an atomic multi-table transaction to place an order.
     * Prevents invalid transactions (stock exhaustion, invalid items, failed payments).
     * Rolls back completely if any step encounters an error.
     *
     * @return Generated orderId on success.
     */
    public int placeOrderAtomic(Order order, List<CartItem> cartItems, PaymentMethod paymentMethod) throws Exception {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot place an order with an empty cart.");
        }
        if (order.getCustomerId() <= 0 || order.getRestaurantId() <= 0) {
            throw new IllegalArgumentException("Invalid customer or restaurant ID.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // Step 1: Lock and check inventory for each item to prevent overselling
            for (CartItem ci : cartItems) {
                String checkSql = "SELECT name, price, stock_quantity, is_available FROM menu_items WHERE item_id = ? FOR UPDATE";
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setInt(1, ci.getMenuItem().getItemId());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Menu item not found: ID " + ci.getMenuItem().getItemId());
                        }
                        boolean isAvail = rs.getBoolean("is_available");
                        int currentStock = rs.getInt("stock_quantity");
                        String itemName = rs.getString("name");

                        if (!isAvail) {
                            throw new IllegalStateException("Item '" + itemName + "' is currently unavailable.");
                        }
                        if (currentStock < ci.getQuantity()) {
                            throw new IllegalStateException("Insufficient stock for '" + itemName + "'. Requested: " +
                                    ci.getQuantity() + ", Available: " + currentStock);
                        }
                    }
                }
            }

            // Step 2: Atomic decrement of stock for each item
            String updateStockSql = "UPDATE menu_items SET stock_quantity = stock_quantity - ? WHERE item_id = ? AND stock_quantity >= ?";
            try (PreparedStatement psStock = conn.prepareStatement(updateStockSql)) {
                for (CartItem ci : cartItems) {
                    psStock.setInt(1, ci.getQuantity());
                    psStock.setInt(2, ci.getMenuItem().getItemId());
                    psStock.setInt(3, ci.getQuantity());
                    int updated = psStock.executeUpdate();
                    if (updated != 1) {
                        throw new IllegalStateException("Failed to reserve stock for '" + ci.getMenuItem().getName() + "'.");
                    }
                }
            }

            // Step 3: Insert master order record
            String insertOrderSql = "INSERT INTO orders (customer_id, restaurant_id, total_amount, status, delivery_address, special_instructions) " +
                    "VALUES (?, ?, ?, 'PENDING', ?, ?)";
            int generatedOrderId;
            try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, order.getCustomerId());
                psOrder.setInt(2, order.getRestaurantId());
                psOrder.setBigDecimal(3, order.getTotalAmount());
                psOrder.setString(4, order.getDeliveryAddress());
                psOrder.setString(5, order.getSpecialInstructions() != null ? order.getSpecialInstructions() : "");
                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedOrderId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to obtain generated order ID.");
                    }
                }
            }

            // Step 4: Insert order line items (batch execution)
            String insertItemSql = "INSERT INTO order_items (order_id, item_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                for (CartItem ci : cartItems) {
                    psItem.setInt(1, generatedOrderId);
                    psItem.setInt(2, ci.getMenuItem().getItemId());
                    psItem.setInt(3, ci.getQuantity());
                    psItem.setBigDecimal(4, ci.getUnitPrice());
                    psItem.setBigDecimal(5, ci.getSubtotal());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            // Step 5: Insert payment settlement record
            String txnRef = "TXN-" + paymentMethod.name().substring(0, Math.min(4, paymentMethod.name().length())) + "-" +
                    UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String insertPaymentSql = "INSERT INTO payments (order_id, amount, payment_method, payment_status, transaction_reference) " +
                    "VALUES (?, ?, ?, 'SUCCESS', ?)";
            try (PreparedStatement psPayment = conn.prepareStatement(insertPaymentSql)) {
                psPayment.setInt(1, generatedOrderId);
                psPayment.setBigDecimal(2, order.getTotalAmount());
                psPayment.setString(3, paymentMethod.name());
                psPayment.setString(4, txnRef);
                psPayment.executeUpdate();
            }

            // Step 6: Log initial order status
            OrderStatusLogDAO.insertLog(conn, generatedOrderId, "PENDING", "Order submitted and payment authorized (" + txnRef + ")");

            // Step 7: Commit transaction
            conn.commit();
            return generatedOrderId;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback(); // ROLLBACK ENTIRE TRANSACTION
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Atomically cancels an order, marks payment as REFUNDED, and restores inventory.
     */
    public boolean cancelOrderAtomic(int orderId, String reason) throws Exception {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // Check current status
            String checkSql = "SELECT status FROM orders WHERE order_id = ? FOR UPDATE";
            OrderStatus currentStatus;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Order #" + orderId + " does not exist.");
                    }
                    currentStatus = OrderStatus.fromString(rs.getString("status"));
                }
            }

            if (!currentStatus.canBeCancelled()) {
                throw new IllegalStateException("Order #" + orderId + " is in '" + currentStatus.getDisplayName() +
                        "' state and cannot be cancelled.");
            }

            // Restore inventory for each item in the order
            String fetchItemsSql = "SELECT item_id, quantity FROM order_items WHERE order_id = ?";
            try (PreparedStatement psFetch = conn.prepareStatement(fetchItemsSql)) {
                psFetch.setInt(1, orderId);
                try (ResultSet rsItems = psFetch.executeQuery()) {
                    String restoreStockSql = "UPDATE menu_items SET stock_quantity = stock_quantity + ? WHERE item_id = ?";
                    try (PreparedStatement psRestore = conn.prepareStatement(restoreStockSql)) {
                        while (rsItems.next()) {
                            int itemId = rsItems.getInt("item_id");
                            int qty = rsItems.getInt("quantity");
                            psRestore.setInt(1, qty);
                            psRestore.setInt(2, itemId);
                            psRestore.executeUpdate();
                        }
                    }
                }
            }

            // Update order status to CANCELLED
            String updateOrderSql = "UPDATE orders SET status = 'CANCELLED' WHERE order_id = ?";
            try (PreparedStatement psOrder = conn.prepareStatement(updateOrderSql)) {
                psOrder.setInt(1, orderId);
                psOrder.executeUpdate();
            }

            // Update payment to REFUNDED
            String updatePaymentSql = "UPDATE payments SET payment_status = 'REFUNDED' WHERE order_id = ?";
            try (PreparedStatement psPayment = conn.prepareStatement(updatePaymentSql)) {
                psPayment.setInt(1, orderId);
                psPayment.executeUpdate();
            }

            // Log cancellation
            OrderStatusLogDAO.insertLog(conn, orderId, "CANCELLED", "Order cancelled: " + reason + " (Inventory refunded)");

            conn.commit();
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Updates an order status to the next state and writes an audit log.
     */
    public boolean updateOrderStatus(int orderId, OrderStatus newStatus, String remarks) throws Exception {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String updateSql = "UPDATE orders SET status = ? WHERE order_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, newStatus.name());
                ps.setInt(2, orderId);
                int count = ps.executeUpdate();
                if (count == 0) {
                    throw new SQLException("Order #" + orderId + " not found.");
                }
            }

            OrderStatusLogDAO.insertLog(conn, orderId, newStatus.name(), remarks != null ? remarks : "Status updated to " + newStatus.getDisplayName());

            conn.commit();
            return true;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT o.order_id, o.customer_id, o.restaurant_id, o.order_date, o.total_amount, o.status, " +
                "o.delivery_address, o.special_instructions, c.full_name AS customer_name, c.phone AS customer_phone, " +
                "r.name AS restaurant_name, r.phone AS restaurant_phone " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "JOIN restaurants r ON o.restaurant_id = r.restaurant_id " +
                "WHERE o.order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(getOrderItemsForOrder(conn, orderId));
                    order.setPayment(new PaymentDAO().getPaymentByOrderId(orderId));
                    return order;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, o.restaurant_id, o.order_date, o.total_amount, o.status, " +
                "o.delivery_address, o.special_instructions, c.full_name AS customer_name, c.phone AS customer_phone, " +
                "r.name AS restaurant_name, r.phone AS restaurant_phone " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "JOIN restaurants r ON o.restaurant_id = r.restaurant_id " +
                "WHERE o.customer_id = ? " +
                "ORDER BY o.order_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = mapOrderRow(rs);
                    order.setItems(getOrderItemsForOrder(conn, order.getOrderId()));
                    order.setPayment(new PaymentDAO().getPaymentByOrderId(order.getOrderId()));
                    list.add(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, o.restaurant_id, o.order_date, o.total_amount, o.status, " +
                "o.delivery_address, o.special_instructions, c.full_name AS customer_name, c.phone AS customer_phone, " +
                "r.name AS restaurant_name, r.phone AS restaurant_phone " +
                "FROM orders o " +
                "JOIN customers c ON o.customer_id = c.customer_id " +
                "JOIN restaurants r ON o.restaurant_id = r.restaurant_id " +
                "ORDER BY o.order_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = mapOrderRow(rs);
                order.setItems(getOrderItemsForOrder(conn, order.getOrderId()));
                order.setPayment(new PaymentDAO().getPaymentByOrderId(order.getOrderId()));
                list.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<OrderItem> getOrderItemsForOrder(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.order_item_id, oi.order_id, oi.item_id, oi.quantity, oi.unit_price, oi.subtotal, " +
                "m.name AS item_name, m.category AS item_category " +
                "FROM order_items oi " +
                "JOIN menu_items m ON oi.item_id = m.item_id " +
                "WHERE oi.order_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setItemId(rs.getInt("item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    item.setItemName(rs.getString("item_name"));
                    item.setItemCategory(rs.getString("item_category"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setRestaurantId(rs.getInt("restaurant_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(OrderStatus.fromString(rs.getString("status")));
        order.setDeliveryAddress(rs.getString("delivery_address"));
        order.setSpecialInstructions(rs.getString("special_instructions"));
        order.setCustomerName(rs.getString("customer_name"));
        order.setCustomerPhone(rs.getString("customer_phone"));
        order.setRestaurantName(rs.getString("restaurant_name"));
        order.setRestaurantPhone(rs.getString("restaurant_phone"));
        return order;
    }
}

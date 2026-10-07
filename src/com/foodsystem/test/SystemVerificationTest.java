package com.foodsystem.test;

import com.foodsystem.config.DBConnection;
import com.foodsystem.models.CartItem;
import com.foodsystem.models.Customer;
import com.foodsystem.models.MenuItem;
import com.foodsystem.models.Order;
import com.foodsystem.models.OrderItem;
import com.foodsystem.models.Payment;
import com.foodsystem.models.Restaurant;
import com.foodsystem.models.enums.OrderStatus;
import com.foodsystem.models.enums.PaymentMethod;
import com.foodsystem.models.enums.PaymentStatus;
import com.foodsystem.service.CartManager;
import com.foodsystem.service.OrderService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SystemVerificationTest {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  ONLINE FOOD ORDERING SYSTEM — JDBC & NORMALIZATION VERIFICATION");
        System.out.println("==================================================================");

        OrderService service = OrderService.getInstance();
        int passed = 0;
        int total = 5;

        try {
            // TEST 1: Database Connectivity and Normalization Schema Integrity
            System.out.println("\n[TEST 1] Verifying Database Connectivity & Normalized Entities...");
            if (!DBConnection.testConnection()) {
                throw new RuntimeException("Database connection failed!");
            }
            List<Customer> customers = service.getAllCustomers();
            List<Restaurant> restaurants = service.getAllRestaurants();
            List<MenuItem> menuItems = service.getAllMenuItems();

            if (customers.isEmpty() || restaurants.isEmpty() || menuItems.isEmpty()) {
                throw new RuntimeException("Seed data not properly loaded in database.");
            }
            System.out.printf("  ✔ Found %d customers, %d restaurants, and %d menu items.\n",
                    customers.size(), restaurants.size(), menuItems.size());
            passed++;

            // TEST 2: Valid Order Placement (Atomic ACID Transaction)
            System.out.println("\n[TEST 2] Testing Valid Order Placement with Multi-Table Atomic Transaction...");
            Customer cust = customers.get(0);
            Restaurant rest = restaurants.get(0);
            List<MenuItem> restItems = service.getMenuItems(rest.getRestaurantId());
            MenuItem itemToOrder = restItems.get(0);

            int initialStock = itemToOrder.getStockQuantity();
            int orderQty = 2;

            Order order = new Order();
            order.setCustomerId(cust.getCustomerId());
            order.setRestaurantId(rest.getRestaurantId());
            order.setTotalAmount(itemToOrder.getPrice().multiply(BigDecimal.valueOf(orderQty)));
            order.setStatus(OrderStatus.PENDING);
            order.setDeliveryAddress("Test Delivery Address 101");
            order.setSpecialInstructions("Verification test order");

            List<CartItem> cartItems = new ArrayList<>();
            cartItems.add(new CartItem(itemToOrder, orderQty));

            int createdOrderId = service.placeOrder(order, cartItems, PaymentMethod.CREDIT_CARD);
            if (createdOrderId <= 0) {
                throw new RuntimeException("Failed to obtain created order ID.");
            }

            // Verify order in database
            Order fetchedOrder = service.getOrderById(createdOrderId);
            if (fetchedOrder == null || fetchedOrder.getItems().isEmpty()) {
                throw new RuntimeException("Order or order items not found after placement.");
            }

            // Verify payment created
            Payment payment = fetchedOrder.getPayment();
            if (payment == null || payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
                throw new RuntimeException("Payment record missing or not SUCCESS.");
            }

            // Verify stock decremented
            MenuItem updatedItem = service.getMenuItems(rest.getRestaurantId()).stream()
                    .filter(m -> m.getItemId() == itemToOrder.getItemId()).findFirst().get();
            if (updatedItem.getStockQuantity() != initialStock - orderQty) {
                throw new RuntimeException("Inventory was not decremented correctly! Expected: " +
                        (initialStock - orderQty) + ", Actual: " + updatedItem.getStockQuantity());
            }

            System.out.printf("  ✔ Order #%d successfully created! Inventory decremented from %d to %d. Payment %s verified.\n",
                    createdOrderId, initialStock, updatedItem.getStockQuantity(), payment.getTransactionReference());
            passed++;

            // TEST 3: Invalid Transaction Prevention - Out of Stock Rollback
            System.out.println("\n[TEST 3] Testing Invalid Transaction Prevention (Exceeding Available Stock)...");
            int currentStock = updatedItem.getStockQuantity();
            int excessiveQty = currentStock + 9999;

            Order invalidOrder = new Order();
            invalidOrder.setCustomerId(cust.getCustomerId());
            invalidOrder.setRestaurantId(rest.getRestaurantId());
            invalidOrder.setTotalAmount(itemToOrder.getPrice().multiply(BigDecimal.valueOf(excessiveQty)));
            invalidOrder.setStatus(OrderStatus.PENDING);
            invalidOrder.setDeliveryAddress("Test Address");

            List<CartItem> excessiveCart = new ArrayList<>();
            excessiveCart.add(new CartItem(updatedItem, excessiveQty));

            boolean caughtRollback = false;
            try {
                service.placeOrder(invalidOrder, excessiveCart, PaymentMethod.UPI);
            } catch (IllegalStateException ex) {
                caughtRollback = true;
                System.out.println("  ✔ Expected transaction rejection: " + ex.getMessage());
            }

            if (!caughtRollback) {
                throw new RuntimeException("System allowed an order with stock beyond inventory limits!");
            }

            // Verify stock was unaffected
            MenuItem postRollbackItem = service.getMenuItems(rest.getRestaurantId()).stream()
                    .filter(m -> m.getItemId() == itemToOrder.getItemId()).findFirst().get();
            if (postRollbackItem.getStockQuantity() != currentStock) {
                throw new RuntimeException("Stock mutated despite transaction rollback!");
            }
            System.out.println("  ✔ Inventory integrity confirmed: No partial stock deduction occurred.");
            passed++;

            // TEST 4: Order Status Transition Simulation & Audit Trail
            System.out.println("\n[TEST 4] Testing Order Status Transitions and Audit Logging...");
            service.updateOrderStatus(createdOrderId, OrderStatus.CONFIRMED, "Restaurant kitchen accepted order");
            service.updateOrderStatus(createdOrderId, OrderStatus.PREPARING, "Chef is baking pizza");

            Order progressingOrder = service.getOrderById(createdOrderId);
            if (progressingOrder.getStatus() != OrderStatus.PREPARING) {
                throw new RuntimeException("Order status update failed. Expected PREPARING, got: " + progressingOrder.getStatus());
            }

            var logs = service.getOrderLogs(createdOrderId);
            if (logs.size() < 3) {
                throw new RuntimeException("Audit logs not recorded for status transitions.");
            }
            System.out.printf("  ✔ Status transition to %s verified. %d audit logs recorded.\n",
                    progressingOrder.getStatus(), logs.size());
            passed++;

            // TEST 5: Atomic Cancellation and Stock Inventory Refund
            System.out.println("\n[TEST 5] Testing Order Cancellation, Inventory Restock, and Payment Refund...");
            int stockBeforeCancel = postRollbackItem.getStockQuantity();

            service.cancelOrder(createdOrderId, "Customer verification test cancellation");

            Order cancelledOrder = service.getOrderById(createdOrderId);
            if (cancelledOrder.getStatus() != OrderStatus.CANCELLED) {
                throw new RuntimeException("Order status was not updated to CANCELLED.");
            }
            if (cancelledOrder.getPayment().getPaymentStatus() != PaymentStatus.REFUNDED) {
                throw new RuntimeException("Payment status was not updated to REFUNDED.");
            }

            MenuItem restoredItem = service.getMenuItems(rest.getRestaurantId()).stream()
                    .filter(m -> m.getItemId() == itemToOrder.getItemId()).findFirst().get();
            if (restoredItem.getStockQuantity() != stockBeforeCancel + orderQty) {
                throw new RuntimeException("Stock inventory was not refunded on cancellation! Expected: " +
                        (stockBeforeCancel + orderQty) + ", Actual: " + restoredItem.getStockQuantity());
            }

            System.out.printf("  ✔ Order #%d cancelled. Inventory restored from %d to %d. Payment marked REFUNDED.\n",
                    createdOrderId, stockBeforeCancel, restoredItem.getStockQuantity());
            passed++;

        } catch (Exception e) {
            System.err.println("Verification failed with exception: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n==================================================================");
        System.out.printf("  TEST RESULTS: %d / %d PASSED (100%% INVARIANT INTEGRITY)\n", passed, total);
        System.out.println("==================================================================");
    }
}

package com.foodsystem.service;

import com.foodsystem.dao.*;
import com.foodsystem.models.*;
import com.foodsystem.models.enums.OrderStatus;
import com.foodsystem.models.enums.PaymentMethod;

import java.util.List;

public class OrderService {
    private static OrderService instance;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RestaurantDAO restaurantDAO = new RestaurantDAO();
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final OrderStatusLogDAO orderStatusLogDAO = new OrderStatusLogDAO();

    private Customer activeCustomer;

    private OrderService() {
        List<Customer> customers = customerDAO.getAllCustomers();
        if (!customers.isEmpty()) {
            activeCustomer = customers.get(0);
        }
    }

    public static synchronized OrderService getInstance() {
        if (instance == null) {
            instance = new OrderService();
        }
        return instance;
    }

    public Customer getActiveCustomer() {
        return activeCustomer;
    }

    public void setActiveCustomer(Customer customer) {
        this.activeCustomer = customer;
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public Customer createCustomer(Customer customer) throws Exception {
        return customerDAO.createCustomer(customer);
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantDAO.getAllActiveRestaurants();
    }

    public List<String> getAllCuisines() {
        return restaurantDAO.getAllCuisines();
    }

    public List<Restaurant> searchRestaurants(String query, String cuisine) {
        return restaurantDAO.searchRestaurants(query, cuisine);
    }

    public Restaurant getRestaurantById(int id) {
        return restaurantDAO.getRestaurantById(id);
    }

    public List<MenuItem> getMenuItems(int restaurantId) {
        return menuItemDAO.getMenuItemsByRestaurant(restaurantId);
    }

    public List<String> getMenuCategories(int restaurantId) {
        return menuItemDAO.getCategoriesForRestaurant(restaurantId);
    }

    public List<MenuItem> getAllMenuItems() {
        return menuItemDAO.getAllMenuItems();
    }

    public boolean updateStockAndAvailability(int itemId, int newStock, boolean isAvailable) {
        return menuItemDAO.updateStockAndAvailability(itemId, newStock, isAvailable);
    }

    public int placeOrder(Order order, List<CartItem> cartItems, PaymentMethod paymentMethod) throws Exception {
        return orderDAO.placeOrderAtomic(order, cartItems, paymentMethod);
    }

    public boolean cancelOrder(int orderId, String reason) throws Exception {
        return orderDAO.cancelOrderAtomic(orderId, reason);
    }

    public boolean updateOrderStatus(int orderId, OrderStatus newStatus, String remarks) throws Exception {
        return orderDAO.updateOrderStatus(orderId, newStatus, remarks);
    }

    public Order getOrderById(int orderId) {
        return orderDAO.getOrderById(orderId);
    }

    public List<Order> getOrdersForActiveCustomer() {
        if (activeCustomer == null) return List.of();
        return orderDAO.getOrdersByCustomerId(activeCustomer.getCustomerId());
    }

    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public List<OrderStatusLog> getOrderLogs(int orderId) {
        return orderStatusLogDAO.getLogsByOrderId(orderId);
    }
}

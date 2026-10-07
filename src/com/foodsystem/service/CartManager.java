package com.foodsystem.service;

import com.foodsystem.models.CartItem;
import com.foodsystem.models.MenuItem;
import com.foodsystem.models.Restaurant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;

    private Restaurant currentRestaurant;
    private final List<CartItem> items = new ArrayList<>();
    private final List<CartChangeListener> listeners = new ArrayList<>();

    public interface CartChangeListener {
        void onCartChanged();
    }

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addListener(CartChangeListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (CartChangeListener l : listeners) {
            l.onCartChanged();
        }
    }

    /**
     * Attempts to add an item to the cart.
     * If cart contains items from another restaurant, returns false to prompt user.
     */
    public boolean addItem(Restaurant restaurant, MenuItem menuItem, int quantity) {
        if (currentRestaurant == null || items.isEmpty()) {
            currentRestaurant = restaurant;
        } else if (currentRestaurant.getRestaurantId() != restaurant.getRestaurantId()) {
            return false; // Mismatched restaurant
        }

        // Check if item already exists in cart
        for (CartItem ci : items) {
            if (ci.getMenuItem().getItemId() == menuItem.getItemId()) {
                int newQty = ci.getQuantity() + quantity;
                if (newQty > menuItem.getStockQuantity()) {
                    newQty = menuItem.getStockQuantity();
                }
                ci.setQuantity(newQty);
                notifyListeners();
                return true;
            }
        }

        items.add(new CartItem(menuItem, quantity));
        notifyListeners();
        return true;
    }

    public void updateQuantity(int itemId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(itemId);
            return;
        }
        for (CartItem ci : items) {
            if (ci.getMenuItem().getItemId() == itemId) {
                if (newQuantity > ci.getMenuItem().getStockQuantity()) {
                    newQuantity = ci.getMenuItem().getStockQuantity();
                }
                ci.setQuantity(newQuantity);
                break;
            }
        }
        notifyListeners();
    }

    public void removeItem(int itemId) {
        items.removeIf(ci -> ci.getMenuItem().getItemId() == itemId);
        if (items.isEmpty()) {
            currentRestaurant = null;
        }
        notifyListeners();
    }

    public void clearCart() {
        items.clear();
        currentRestaurant = null;
        notifyListeners();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public Restaurant getCurrentRestaurant() {
        return currentRestaurant;
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem ci : items) {
            count += ci.getQuantity();
        }
        return count;
    }

    public BigDecimal getSubtotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : items) {
            total = total.add(ci.getSubtotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDeliveryFee() {
        if (items.isEmpty()) return BigDecimal.ZERO;
        BigDecimal subtotal = getSubtotal();
        // Free delivery over $35.00
        if (subtotal.compareTo(new BigDecimal("35.00")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("3.99");
    }

    public BigDecimal getTax() {
        BigDecimal subtotal = getSubtotal();
        return subtotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP); // 5% tax
    }

    public BigDecimal getGrandTotal() {
        if (items.isEmpty()) return BigDecimal.ZERO;
        return getSubtotal().add(getDeliveryFee()).add(getTax()).setScale(2, RoundingMode.HALF_UP);
    }
}

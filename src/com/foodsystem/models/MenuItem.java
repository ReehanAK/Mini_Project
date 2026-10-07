package com.foodsystem.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MenuItem {
    private int itemId;
    private int restaurantId;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private boolean isAvailable;
    private int stockQuantity;
    private Timestamp createdAt;

    public MenuItem() {}

    public MenuItem(int itemId, int restaurantId, String name, String description,
                    String category, BigDecimal price, boolean isAvailable, int stockQuantity) {
        this.itemId = itemId;
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAvailable = isAvailable;
        this.stockQuantity = stockQuantity;
    }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getRestaurantId() { return restaurantId; }
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isAvailable() { return isAvailable && stockQuantity > 0; }
    public boolean getRawIsAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " - $" + price + " (Stock: " + stockQuantity + ")";
    }
}

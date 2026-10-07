package com.foodsystem.models;

import java.sql.Timestamp;

public class Restaurant {
    private int restaurantId;
    private String name;
    private String cuisineType;
    private String address;
    private String phone;
    private double rating;
    private boolean isActive;
    private Timestamp createdAt;

    public Restaurant() {}

    public Restaurant(int restaurantId, String name, String cuisineType, String address, String phone, double rating, boolean isActive) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.cuisineType = cuisineType;
        this.address = address;
        this.phone = phone;
        this.rating = rating;
        this.isActive = isActive;
    }

    public int getRestaurantId() { return restaurantId; }
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + cuisineType + " - ⭐ " + rating + ")";
    }
}

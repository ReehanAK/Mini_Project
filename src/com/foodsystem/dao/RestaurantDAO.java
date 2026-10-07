package com.foodsystem.dao;

import com.foodsystem.config.DBConnection;
import com.foodsystem.models.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RestaurantDAO {

    public List<Restaurant> getAllActiveRestaurants() {
        return searchRestaurants(null, null);
    }

    public List<String> getAllCuisines() {
        List<String> cuisines = new ArrayList<>();
        cuisines.add("All Cuisines");
        String sql = "SELECT DISTINCT cuisine_type FROM restaurants WHERE is_active = TRUE ORDER BY cuisine_type ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cuisines.add(rs.getString("cuisine_type"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cuisines;
    }

    public List<Restaurant> searchRestaurants(String query, String cuisine) {
        List<Restaurant> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT restaurant_id, name, cuisine_type, address, phone, rating, is_active, created_at FROM restaurants WHERE is_active = TRUE ");

        boolean hasQuery = (query != null && !query.trim().isEmpty());
        boolean hasCuisine = (cuisine != null && !cuisine.equalsIgnoreCase("All Cuisines") && !cuisine.trim().isEmpty());

        if (hasQuery) {
            sql.append(" AND (name LIKE ? OR cuisine_type LIKE ? OR address LIKE ?) ");
        }
        if (hasCuisine) {
            sql.append(" AND cuisine_type = ? ");
        }
        sql.append(" ORDER BY rating DESC, name ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIdx = 1;
            if (hasQuery) {
                String likeTerm = "%" + query.trim() + "%";
                ps.setString(paramIdx++, likeTerm);
                ps.setString(paramIdx++, likeTerm);
                ps.setString(paramIdx++, likeTerm);
            }
            if (hasCuisine) {
                ps.setString(paramIdx++, cuisine.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Restaurant r = new Restaurant();
                    r.setRestaurantId(rs.getInt("restaurant_id"));
                    r.setName(rs.getString("name"));
                    r.setCuisineType(rs.getString("cuisine_type"));
                    r.setAddress(rs.getString("address"));
                    r.setPhone(rs.getString("phone"));
                    r.setRating(rs.getDouble("rating"));
                    r.setActive(rs.getBoolean("is_active"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Restaurant getRestaurantById(int restaurantId) {
        String sql = "SELECT restaurant_id, name, cuisine_type, address, phone, rating, is_active, created_at FROM restaurants WHERE restaurant_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Restaurant r = new Restaurant();
                    r.setRestaurantId(rs.getInt("restaurant_id"));
                    r.setName(rs.getString("name"));
                    r.setCuisineType(rs.getString("cuisine_type"));
                    r.setAddress(rs.getString("address"));
                    r.setPhone(rs.getString("phone"));
                    r.setRating(rs.getDouble("rating"));
                    r.setActive(rs.getBoolean("is_active"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    return r;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.foodsystem.dao;

import com.foodsystem.config.DBConnection;
import com.foodsystem.models.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {

    public List<MenuItem> getMenuItemsByRestaurant(int restaurantId) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT item_id, restaurant_id, name, description, category, price, is_available, stock_quantity, created_at " +
                     "FROM menu_items WHERE restaurant_id = ? ORDER BY category ASC, name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MenuItem item = mapRow(rs);
                    list.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getCategoriesForRestaurant(int restaurantId) {
        List<String> list = new ArrayList<>();
        list.add("All Categories");
        String sql = "SELECT DISTINCT category FROM menu_items WHERE restaurant_id = ? ORDER BY category ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("category"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public MenuItem getMenuItemById(int itemId) {
        String sql = "SELECT item_id, restaurant_id, name, description, category, price, is_available, stock_quantity, created_at " +
                     "FROM menu_items WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT item_id, restaurant_id, name, description, category, price, is_available, stock_quantity, created_at " +
                     "FROM menu_items ORDER BY restaurant_id ASC, category ASC, name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStockAndAvailability(int itemId, int newStock, boolean isAvailable) {
        String sql = "UPDATE menu_items SET stock_quantity = ?, is_available = ? WHERE item_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Math.max(0, newStock));
            ps.setBoolean(2, isAvailable);
            ps.setInt(3, itemId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private MenuItem mapRow(ResultSet rs) throws Exception {
        MenuItem item = new MenuItem();
        item.setItemId(rs.getInt("item_id"));
        item.setRestaurantId(rs.getInt("restaurant_id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setCategory(rs.getString("category"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setAvailable(rs.getBoolean("is_available"));
        item.setStockQuantity(rs.getInt("stock_quantity"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        return item;
    }
}

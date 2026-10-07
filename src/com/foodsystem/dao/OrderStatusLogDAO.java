package com.foodsystem.dao;

import com.foodsystem.config.DBConnection;
import com.foodsystem.models.OrderStatusLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderStatusLogDAO {

    public List<OrderStatusLog> getLogsByOrderId(int orderId) {
        List<OrderStatusLog> list = new ArrayList<>();
        String sql = "SELECT log_id, order_id, status, remarks, updated_at " +
                     "FROM order_status_logs WHERE order_id = ? ORDER BY updated_at ASC, log_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderStatusLog log = new OrderStatusLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setOrderId(rs.getInt("order_id"));
                    log.setStatus(rs.getString("status"));
                    log.setRemarks(rs.getString("remarks"));
                    log.setUpdatedAt(rs.getTimestamp("updated_at"));
                    list.add(log);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void insertLog(Connection conn, int orderId, String status, String remarks) throws Exception {
        String sql = "INSERT INTO order_status_logs (order_id, status, remarks) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setString(2, status);
            ps.setString(3, remarks);
            ps.executeUpdate();
        }
    }
}

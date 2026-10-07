package com.foodsystem.dao;

import com.foodsystem.config.DBConnection;
import com.foodsystem.models.Payment;
import com.foodsystem.models.enums.PaymentMethod;
import com.foodsystem.models.enums.PaymentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PaymentDAO {

    public Payment getPaymentByOrderId(int orderId) {
        String sql = "SELECT payment_id, order_id, payment_date, amount, payment_method, payment_status, transaction_reference " +
                     "FROM payments WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Payment p = new Payment();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setOrderId(rs.getInt("order_id"));
                    p.setPaymentDate(rs.getTimestamp("payment_date"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    p.setPaymentMethod(PaymentMethod.fromString(rs.getString("payment_method")));
                    p.setPaymentStatus(PaymentStatus.fromString(rs.getString("payment_status")));
                    p.setTransactionReference(rs.getString("transaction_reference"));
                    return p;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePaymentStatus(int paymentId, PaymentStatus status) {
        String sql = "UPDATE payments SET payment_status = ? WHERE payment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, paymentId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

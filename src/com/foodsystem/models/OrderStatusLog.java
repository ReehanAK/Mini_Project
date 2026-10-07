package com.foodsystem.models;

import java.sql.Timestamp;

public class OrderStatusLog {
    private int logId;
    private int orderId;
    private String status;
    private String remarks;
    private Timestamp updatedAt;

    public OrderStatusLog() {}

    public OrderStatusLog(int logId, int orderId, String status, String remarks, Timestamp updatedAt) {
        this.logId = logId;
        this.orderId = orderId;
        this.status = status;
        this.remarks = remarks;
        this.updatedAt = updatedAt;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}

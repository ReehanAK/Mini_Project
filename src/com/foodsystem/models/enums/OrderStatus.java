package com.foodsystem.models.enums;

public enum OrderStatus {
    PENDING("Pending", "#EAB308"),           // Amber
    CONFIRMED("Confirmed", "#3B82F6"),       // Blue
    PREPARING("Preparing", "#8B5CF6"),       // Purple
    OUT_FOR_DELIVERY("Out For Delivery", "#06B6D4"), // Cyan
    DELIVERED("Delivered", "#10B981"),       // Green
    CANCELLED("Cancelled", "#EF4444");       // Red

    private final String displayName;
    private final String colorHex;

    OrderStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() { return displayName; }
    public String getColorHex() { return colorHex; }

    public static OrderStatus fromString(String text) {
        if (text == null) return PENDING;
        for (OrderStatus b : OrderStatus.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return PENDING;
    }

    public OrderStatus next() {
        switch (this) {
            case PENDING: return CONFIRMED;
            case CONFIRMED: return PREPARING;
            case PREPARING: return OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY: return DELIVERED;
            default: return this;
        }
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == CONFIRMED || this == PREPARING;
    }
}

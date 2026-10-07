package com.foodsystem.models.enums;

public enum PaymentStatus {
    PENDING("Pending", "#EAB308"),
    SUCCESS("Success", "#10B981"),
    FAILED("Failed", "#EF4444"),
    REFUNDED("Refunded", "#6B7280");

    private final String displayName;
    private final String colorHex;

    PaymentStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() { return displayName; }
    public String getColorHex() { return colorHex; }

    public static PaymentStatus fromString(String text) {
        if (text == null) return PENDING;
        for (PaymentStatus s : PaymentStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return PENDING;
    }
}

package com.foodsystem.models.enums;

public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    UPI("UPI / QR Code"),
    CASH_ON_DELIVERY("Cash on Delivery (COD)"),
    NET_BANKING("Net Banking");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static PaymentMethod fromString(String text) {
        if (text == null) return CASH_ON_DELIVERY;
        for (PaymentMethod m : PaymentMethod.values()) {
            if (m.name().equalsIgnoreCase(text) || m.displayName.equalsIgnoreCase(text)) {
                return m;
            }
        }
        return CASH_ON_DELIVERY;
    }
}

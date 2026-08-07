package com.example.pharmacymanagement.enums;

public enum PaymentMethod { // NEWLY ADDED
    CASH("Cash on Delivery"),
    CARD("Card / POS"),
    BKASH("bKash"),
    NAGAD("Nagad"),
    ROCKET("Rocket"),
    BANK_TRANSFER("Bank Transfer");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

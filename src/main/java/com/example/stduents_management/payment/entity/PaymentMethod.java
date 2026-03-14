package com.example.stduents_management.payment.entity;

public enum PaymentMethod {

    BANK_TRANSFER("Chuyển khoản"),
    CASH("Tiền mặt"),
    E_WALLET("Ví điện tử"),
    ONLINE_PAYMENT("Thanh toán trực tuyến");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

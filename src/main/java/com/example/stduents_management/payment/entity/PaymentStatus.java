package com.example.stduents_management.payment.entity;

public enum PaymentStatus {

    PENDING("Chờ xử lý"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

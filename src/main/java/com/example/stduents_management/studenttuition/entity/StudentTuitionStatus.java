package com.example.stduents_management.studenttuition.entity;

public enum StudentTuitionStatus {

    UNPAID("Chưa đóng"),
    PARTIAL("Đã đóng một phần"),
    PAID("Đã đóng đủ");

    private final String label;

    StudentTuitionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}


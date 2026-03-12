package com.example.stduents_management.tuitionfee.entity;

public enum TuitionFeeStatus {
    ACTIVE("Đang áp dụng"),
    INACTIVE("Ngừng áp dụng");

    private final String label;

    TuitionFeeStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

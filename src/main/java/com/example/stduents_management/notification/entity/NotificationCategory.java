package com.example.stduents_management.notification.entity;

public enum NotificationCategory {
    TUITION_FEE("Thông báo học phí"),
    EXAM_SCHEDULE("Thông báo lịch thi"),
    SCHEDULE_CHANGE("Thay đổi lịch học"),
    SYSTEM_WARNING("Cảnh báo hệ thống"),
    OTHER("Thông báo khác");

    private final String label;

    NotificationCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}


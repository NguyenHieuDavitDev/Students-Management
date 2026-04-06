package com.example.stduents_management.scheduleoverride.entity;

/** Loại thay đổi lịch: Dạy bù, Đổi phòng, Đổi giờ, Dịch một buổi, Hủy buổi */
public enum OverrideType {
    MAKEUP,       // Dạy bù
    ROOM_CHANGE,  // Đổi phòng
    TIME_CHANGE,  // Đổi khung giờ (cùng ngày)
    RESCHEDULE,   // Dịch buổi sang ngày/tiết khác (một lần; lịch gốc không đổi)
    CANCEL        // Hủy buổi
}

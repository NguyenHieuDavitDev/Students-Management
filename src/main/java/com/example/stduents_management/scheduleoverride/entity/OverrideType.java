package com.example.stduents_management.scheduleoverride.entity;

/** Loại thay đổi lịch: Dạy bù, Đổi phòng, Đổi giờ, Hủy buổi */
public enum OverrideType {
    MAKEUP,       // Dạy bù
    ROOM_CHANGE,  // Đổi phòng
    TIME_CHANGE,  // Đổi khung giờ
    CANCEL        // Hủy buổi
}

package com.example.stduents_management.employee.entity;

/**
 * Phân loại quyết định nhân sự (lưu kèm lịch sử phòng ban / chức danh).
 * {@link #LECTURER_APPOINTMENT}: sự kiện liên quan bổ nhiệm/tuyển dụng giảng viên — đồng bộ bảng lecturers.
 */
public enum DecisionType {
    LECTURER_APPOINTMENT,
    STAFF_APPOINTMENT,
    TRANSFER,
    PROMOTION,
    DISCIPLINE,
    TERMINATION,
    OTHER
}

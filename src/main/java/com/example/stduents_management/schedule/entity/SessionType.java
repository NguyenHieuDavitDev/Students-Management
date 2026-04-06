package com.example.stduents_management.schedule.entity;

import lombok.Getter;

/**
 * Loại buổi trên lịch. {@link #SUPPLEMENTARY}: mỗi buổi lặp theo tuần khi lưu sẽ <strong>rút</strong> một buổi tương ứng
 * từ <strong>cuối</strong> các dòng lý thuyết/thực hành cùng lớp học phần (nghiệp vụ “tăng cường thay buổi cuối”).
 * Vẫn chịu hạn mức 5×TC chung. {@link #EXAM} không tính vào hạn mức.
 */
@Getter
public enum SessionType {
    THEORY("Lý thuyết"),
    PRACTICE("Thực hành"),
    EXAM("Thi / kiểm tra"),
    SUPPLEMENTARY("Tăng cường");

    private final String labelVi;

    SessionType(String labelVi) {
        this.labelVi = labelVi;
    }
}

package com.example.stduents_management.schedule.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AutoScheduleRequest {

    @NotNull(message = "Vui lòng chọn học kỳ")
    private Long semesterId;

    @Min(value = 1, message = "Tuần bắt đầu từ 1")
    @Max(value = 53, message = "Tuần không vượt quá 53")
    private Integer startWeek = 1;

    /**
     * Tuần kết thúc của khung giảng dạy (cùng học kỳ). Để trống hệ thống dùng tuần bắt đầu + (số buổi theo tín chỉ − 1).
     * Nếu có: số buổi (theo tín chỉ) được xếp trong [startWeek, endWeek]; nếu nhiều buổi hơn số tuần trong khung → tự thêm dòng lịch (thứ/tiết khác) trong cùng khung.
     */
    @Min(value = 1, message = "Tuần kết thúc từ 1")
    @Max(value = 53, message = "Tuần không vượt quá 53")
    private Integer endWeek;

    /**
     * Thứ trong tuần được phép xếp (mô hình hệ thống: 2 = Thứ 2 … 8 = Chủ nhật).
     * Thứ tự trên form (T2 → … → CN) = thứ tự ưu tiên khi thử ô trống (sau đó theo thứ tự tiết).
     */
    @NotEmpty(message = "Chọn ít nhất một thứ trong tuần")
    private List<Integer> allowedDayOfWeeks = new ArrayList<>(List.of(2, 3, 4, 5, 6));

    /**
     * Các khung giờ (tiết) được phép dùng khi xếp lịch — thứ tự ưu tiên khi thử ô trống.
     */
    @NotEmpty(message = "Chọn ít nhất một tiết (khung giờ) trong tuần")
    private List<Integer> allowedTimeSlotIds;

    /**
     * Danh sách lớp học phần cần phân lịch.
     * Nếu để trống -> phân lịch cho tất cả lớp học phần đang mở của học kỳ.
     */
    private List<Long> classSectionIds;

    /** Nếu true: xóa toàn bộ lịch học kỳ đó trước khi phân lịch mới */
    private Boolean clearExisting = false;
}

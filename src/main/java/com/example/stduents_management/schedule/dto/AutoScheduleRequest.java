package com.example.stduents_management.schedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AutoScheduleRequest {

    @NotNull(message = "Vui lòng chọn học kỳ")
    private Long semesterId;

    @Min(value = 1, message = "Tuần bắt đầu từ 1")
    private Integer startWeek = 1;

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

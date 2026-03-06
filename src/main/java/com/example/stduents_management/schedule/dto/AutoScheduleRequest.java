package com.example.stduents_management.schedule.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Min(value = 1, message = "Tuần kết thúc từ 1")
    @Max(value = 53, message = "Tuần tối đa 53")
    private Integer endWeek = 15;

    /**
     * Danh sách lớp học phần cần phân lịch.
     * Nếu để trống -> phân lịch cho tất cả lớp học phần đang mở của học kỳ.
     */
    private List<Long> classSectionIds;

    /** Nếu true: xóa toàn bộ lịch học kỳ đó trước khi phân lịch mới */
    private Boolean clearExisting = false;
}

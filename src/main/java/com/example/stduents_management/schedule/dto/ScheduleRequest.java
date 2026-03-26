package com.example.stduents_management.schedule.dto;

import com.example.stduents_management.schedule.entity.ScheduleStatus;
import com.example.stduents_management.schedule.entity.ScheduleType;
import com.example.stduents_management.schedule.entity.SessionType;
import com.example.stduents_management.schedule.entity.WeekPattern;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleRequest {

    @NotNull(message = "Học kỳ không được để trống")
    private Long semesterId;

    @NotNull(message = "Lớp học phần không được để trống")
    private Long classSectionId;

    @NotNull(message = "Giảng viên không được để trống")
    private UUID lecturerId;

    @NotNull(message = "Phòng học không được để trống")
    private Long roomId;

    @NotNull(message = "Khung giờ không được để trống")
    private Integer timeSlotId;

    @NotNull(message = "Thứ không được để trống")
    @Min(value = 2, message = "Thứ từ 2 đến 8")
    @Max(value = 8, message = "Thứ từ 2 đến 8")
    private Integer dayOfWeek;

    @NotNull(message = "Tuần bắt đầu không được để trống")
    @Min(value = 1, message = "Tuần bắt đầu >= 1")
    private Integer startWeek;

    @NotNull(message = "Tuần kết thúc không được để trống")
    @Min(value = 1, message = "Tuần kết thúc >= 1")
    private Integer endWeek;

    @NotNull(message = "Kiểu tuần không được để trống")
    private WeekPattern weekPattern;

    @NotNull(message = "Loại buổi học không được để trống")
    private SessionType sessionType;

    @NotNull(message = "Loại lịch không được để trống")
    private ScheduleType scheduleType;

    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    @Size(max = 255)
    private String note;
}

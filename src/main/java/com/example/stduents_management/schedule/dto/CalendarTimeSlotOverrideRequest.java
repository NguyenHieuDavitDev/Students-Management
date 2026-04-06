package com.example.stduents_management.schedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Đổi tiết cho đúng một ngày (TIME_CHANGE). */
public record CalendarTimeSlotOverrideRequest(
        @NotNull(message = "Ngày buổi học không được để trống")
        LocalDate overrideDate,
        @NotNull(message = "Chọn khung giờ mới")
        Integer newTimeSlotId
) {}

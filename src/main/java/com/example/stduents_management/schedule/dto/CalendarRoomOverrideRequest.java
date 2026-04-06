package com.example.stduents_management.schedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Đổi phòng cho đúng một ngày (bản ghi schedule_overrides, không sửa lịch gốc). */
public record CalendarRoomOverrideRequest(
        @NotNull(message = "Ngày buổi học không được để trống")
        LocalDate overrideDate,
        @NotNull(message = "Chọn phòng mới")
        Long newRoomId
) {}

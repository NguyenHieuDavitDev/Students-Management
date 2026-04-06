package com.example.stduents_management.schedule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Dịch một buổi sang ngày + tiết khác (RESCHEDULE; lịch gốc không đổi). */
public record CalendarRescheduleOnceRequest(
        @NotNull(message = "Ngày buổi gốc không được để trống")
        LocalDate originalDate,
        @NotNull(message = "Ngày đích không được để trống")
        LocalDate movedToDate,
        @NotNull(message = "Chọn khung giờ tại ngày đích")
        Integer newTimeSlotId,
        Long newRoomId
) {}

package com.example.stduents_management.timeslot.dto;

import java.time.LocalTime;

public record TimeSlotResponse(
        Integer id,
        String slotCode,
        Integer periodStart,
        Integer periodEnd,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isActive,
        /** MORNING | AFTERNOON | EVENING — suy từ giờ bắt đầu hoặc số tiết */
        String dayPart,
        String dayPartLabel
) {}

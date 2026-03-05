package com.example.stduents_management.timeslot.dto;

import java.time.LocalTime;

public record TimeSlotResponse(
        Integer id,
        String slotCode,
        Integer periodStart,
        Integer periodEnd,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isActive
) {}

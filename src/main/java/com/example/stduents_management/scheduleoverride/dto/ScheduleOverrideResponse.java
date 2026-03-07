package com.example.stduents_management.scheduleoverride.dto;

import com.example.stduents_management.scheduleoverride.entity.OverrideStatus;
import com.example.stduents_management.scheduleoverride.entity.OverrideType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleOverrideResponse(
        UUID overrideId,
        UUID scheduleId,
        String scheduleBrief,
        LocalDate overrideDate,
        OverrideType overrideType,
        Long newRoomId,
        String newRoomDisplay,
        Integer newTimeSlotId,
        String newTimeSlotDisplay,
        UUID newLecturerId,
        String newLecturerName,
        OverrideStatus status,
        String reason,
        UUID approvedBy,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

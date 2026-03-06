package com.example.stduents_management.schedule.dto;

import com.example.stduents_management.schedule.entity.ScheduleStatus;
import com.example.stduents_management.schedule.entity.ScheduleType;
import com.example.stduents_management.schedule.entity.SessionType;
import com.example.stduents_management.schedule.entity.WeekPattern;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleResponse(
        UUID id,
        Long semesterId,
        String semesterCode,
        String semesterName,
        Long classSectionId,
        String classSectionCode,
        String classSectionName,
        String courseCode,
        String courseName,
        UUID lecturerId,
        String lecturerCode,
        String lecturerName,
        Long roomId,
        String roomCode,
        String roomName,
        Integer timeSlotId,
        String timeSlotCode,
        LocalTime timeSlotStartTime,
        LocalTime timeSlotEndTime,
        Integer dayOfWeek,
        Integer startWeek,
        Integer endWeek,
        WeekPattern weekPattern,
        SessionType sessionType,
        ScheduleType scheduleType,
        ScheduleStatus status,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

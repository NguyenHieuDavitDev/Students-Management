package com.example.stduents_management.examschedule.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ExamScheduleResponse(
        UUID id,

        Long classSectionId,
        String classSectionCode,
        String courseCode,
        String courseName,
        String semesterName,

        UUID examTypeId,
        String examTypeName,

        LocalDate examDate,
        LocalTime startTime,
        Integer durationMinutes,
        String note,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
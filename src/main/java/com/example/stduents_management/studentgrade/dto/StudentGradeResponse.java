package com.example.stduents_management.studentgrade.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StudentGradeResponse(
        UUID id,
        UUID studentId,
        String studentCode,
        String studentName,
        Long courseClassId,
        String classCode,
        String className,
        String courseCode,
        String courseName,
        UUID gradeComponentId,
        String componentName,
        BigDecimal maxScore,
        BigDecimal score,
        UUID gradedByLecturerId,
        String gradedByLecturerName,
        LocalDateTime gradedAt,
        LocalDateTime updatedAt
) {
}

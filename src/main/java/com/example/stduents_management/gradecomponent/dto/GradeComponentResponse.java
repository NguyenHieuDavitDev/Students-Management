package com.example.stduents_management.gradecomponent.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GradeComponentResponse(
        UUID id,
        Long courseClassId,
        String classCode,
        String className,
        String courseCode,
        String courseName,
        String componentName,
        BigDecimal weight,
        BigDecimal maxScore,
        LocalDateTime createdAt
) {
}

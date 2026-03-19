package com.example.stduents_management.graduationresult.dto;

import com.example.stduents_management.graduationresult.entity.GraduationResultStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GraduationResultResponse(
        Long id,
        UUID studentId,
        String studentCode,
        String studentName,
        UUID programId,
        String programCode,
        String programName,
        String majorName,
        String course,
        Integer totalCredits,
        BigDecimal gpa,
        String certificates,
        String missingCourses,
        String note,
        GraduationResultStatus status,
        String statusLabel,
        LocalDateTime checkedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}


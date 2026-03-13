package com.example.stduents_management.studenttuition.dto;

import com.example.stduents_management.studenttuition.entity.StudentTuitionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StudentTuitionResponse(
        UUID id,
        UUID studentId,
        String studentCode,
        String studentName,
        Long semesterId,
        String semesterCode,
        String semesterName,
        Integer totalCredits,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal remainingAmount,
        StudentTuitionStatus status,
        String statusLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}


package com.example.stduents_management.payment.dto;

import com.example.stduents_management.payment.entity.PaymentMethod;
import com.example.stduents_management.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        Long id,
        UUID studentTuitionId,
        String studentCode,
        String studentName,
        Long semesterId,
        String semesterCode,
        String semesterName,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String paymentMethodLabel,
        String transactionCode,
        LocalDateTime paymentDate,
        PaymentStatus status,
        String statusLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

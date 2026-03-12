package com.example.stduents_management.tuitionfee.dto;

import com.example.stduents_management.tuitionfee.entity.TuitionFeeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TuitionFeeResponse(
        UUID id,
        UUID programId,
        String programCode,
        String programName,
        String majorName,
        BigDecimal feePerCredit,
        LocalDate effectiveDate,
        TuitionFeeStatus status,
        String statusLabel,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

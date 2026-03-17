package com.example.stduents_management.graduationcondition.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GraduationConditionResponse(
        Long id,
        UUID programId,
        String programCode,
        String programName,
        String majorName,
        String course,
        Integer minCredits,
        BigDecimal minGpa,
        String requiredCertificate,
        String requiredCourses,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

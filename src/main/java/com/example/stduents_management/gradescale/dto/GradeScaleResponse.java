package com.example.stduents_management.gradescale.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeScaleResponse(
        UUID id,
        BigDecimal minScore,
        BigDecimal maxScore,
        String letterGrade,
        BigDecimal gradePoint,
        String description
) {
}

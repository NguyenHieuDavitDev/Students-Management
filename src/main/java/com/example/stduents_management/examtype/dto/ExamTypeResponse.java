package com.example.stduents_management.examtype.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExamTypeResponse(
        UUID id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

package com.example.stduents_management.semester.dto;

import com.example.stduents_management.semester.entity.SemesterStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SemesterResponse(
        Long id,
        String code,
        String name,
        String academicYear,
        Integer term,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate registrationStart,
        LocalDate registrationEnd,
        SemesterStatus status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
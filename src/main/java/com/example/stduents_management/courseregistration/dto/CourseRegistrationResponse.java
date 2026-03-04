package com.example.stduents_management.courseregistration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseRegistrationResponse(
        Long id,
        UUID studentId,
        String studentCode,
        String studentName,
        Long classSectionId,
        String classCode,
        String className,
        String courseCode,
        String courseName,
        String semesterCode,
        String semesterName,
        LocalDateTime registeredAt,
        String note
) {
}


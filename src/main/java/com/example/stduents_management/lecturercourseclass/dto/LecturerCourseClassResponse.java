package com.example.stduents_management.lecturercourseclass.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LecturerCourseClassResponse(
        Long id,
        Long classSectionId,
        String classCode,
        String className,
        String courseCode,
        String courseName,
        String semesterCode,

        UUID lecturerId,
        String lecturerCode,
        String lecturerName,
        String facultyName,

        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}


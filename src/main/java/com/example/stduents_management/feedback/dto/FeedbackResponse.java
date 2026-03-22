package com.example.stduents_management.feedback.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedbackResponse(
        UUID feedbackId,
        UUID studentId,
        String studentCode,
        String studentName,
        UUID lecturerId,
        String lecturerCode,
        String lecturerName,
        UUID subjectId,
        String courseCode,
        String courseName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}

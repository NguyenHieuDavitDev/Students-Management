package com.example.stduents_management.attendance.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AttendanceResponse(
        UUID attendanceId,
        UUID studentId,
        String studentCode,
        String studentName,
        Long courseClassId,
        String classCode,
        String className,
        String courseCode,
        String courseName,
        LocalDate attendanceDate,
        Boolean present,
        String note,
        UUID lecturerId,
        String lecturerCode,
        String lecturerName,
        LocalDateTime markedAt
) {
}


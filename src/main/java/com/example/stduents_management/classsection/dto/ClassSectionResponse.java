package com.example.stduents_management.classsection.dto;

import com.example.stduents_management.classsection.entity.ClassSectionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClassSectionResponse(
        Long id,
        UUID courseId,
        String courseCode,
        String courseName,
        Long semesterId,
        String semesterCode,
        String semesterName,
        String classCode,
        String className,
        UUID administrativeClassId,
        String administrativeClassCode,
        String administrativeClassName,
        Integer maxStudents,
        Integer currentStudents,
        ClassSectionStatus status,
        Long roomId,
        String roomCode,
        String roomName,
        String note,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

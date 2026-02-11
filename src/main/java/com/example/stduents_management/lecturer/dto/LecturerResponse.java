package com.example.stduents_management.lecturer.dto;

import java.time.LocalDate;
import java.util.UUID;

public record LecturerResponse(
        UUID lecturerId,
        String lecturerCode,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String citizenId,
        String email,
        String phoneNumber,
        String address,
        String avatar,
        UUID positionId,
        String positionName,
        String academicTitle,
        UUID facultyId,
        String facultyName
) {
}

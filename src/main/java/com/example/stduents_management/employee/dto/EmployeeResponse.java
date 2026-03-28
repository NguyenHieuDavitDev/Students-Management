package com.example.stduents_management.employee.dto;

import com.example.stduents_management.employee.entity.EmployeeType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeeResponse(
        UUID employeeId,
        String employeeCode,
        String fullName,
        LocalDate dateOfBirth,
        String gender,
        String citizenId,
        String email,
        String phoneNumber,
        String address,
        String avatar,
        EmployeeType employeeType,
        String status,
        UUID positionId,
        String positionName,
        UUID departmentId,
        String departmentName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}


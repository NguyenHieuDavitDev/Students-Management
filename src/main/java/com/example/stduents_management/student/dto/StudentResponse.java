package com.example.stduents_management.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
public class StudentResponse {

    private UUID studentId;

    private String studentCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String citizenId;

    private String email;
    private String phoneNumber;
    private String address;
    private String avatar;

    private UUID classId;
    private String className;

    private UUID majorId;
    private String majorName;
}

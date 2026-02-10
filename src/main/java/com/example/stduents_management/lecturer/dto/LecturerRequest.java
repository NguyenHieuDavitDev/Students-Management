package com.example.stduents_management.lecturer.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LecturerRequest {

    @NotBlank
    private String lecturerCode;

    @NotBlank
    private String fullName;

    @Past
    private LocalDate dateOfBirth;

    private String gender;
    private String citizenId;

    @Email
    private String email;

    private String phoneNumber;
    private String address;

    private String academicDegree;
    private String academicTitle;

    private MultipartFile avatarFile;
    private String avatar;

    @NotNull
    private UUID facultyId;
}

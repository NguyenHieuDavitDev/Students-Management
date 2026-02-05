package com.example.stduents_management.faculty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FacultyRequest {

    @NotBlank(message = "Faculty code is required")
    @Size(max = 50, message = "Faculty code must be at most 50 characters")
    private String facultyCode;

    @NotBlank(message = "Faculty name is required")
    @Size(max = 150, message = "Faculty name must be at most 150 characters")
    private String facultyName;
}

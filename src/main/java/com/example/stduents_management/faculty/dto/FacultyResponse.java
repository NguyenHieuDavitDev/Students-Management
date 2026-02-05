package com.example.stduents_management.faculty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class FacultyResponse {
    private UUID facultyId;
    private String facultyCode;
    private String facultyName;
}

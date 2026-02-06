package com.example.stduents_management.major.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class MajorResponse {

    private UUID majorId;
    private String majorName;

    private UUID facultyId;
    private String facultyName;
}

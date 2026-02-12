package com.example.stduents_management.trainingprogram.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TrainingProgramResponse {

    private UUID programId;
    private String programCode;
    private String programName;
    private UUID majorId;
    private String majorName;
    private String course;
    private String description;
    private Integer durationYears;
    private Integer totalCredits;
    private Boolean isActive;
}

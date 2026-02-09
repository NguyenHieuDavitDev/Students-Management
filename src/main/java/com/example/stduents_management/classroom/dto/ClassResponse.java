package com.example.stduents_management.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ClassResponse {

    private UUID classId;
    private String classCode;
    private String className;
    private String academicYear;

    private UUID majorId;
    private String majorName;

    private UUID educationTypeId;
    private String educationTypeName;

    private UUID trainingLevelId;
    private String trainingLevelName;

    private Integer maxStudent;
    private String classStatus;
    private Boolean isActive;
}

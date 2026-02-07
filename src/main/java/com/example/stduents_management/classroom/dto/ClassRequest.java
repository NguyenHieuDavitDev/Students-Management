package com.example.stduents_management.classroom.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class ClassRequest {

    @NotBlank
    private String classCode;

    @NotBlank
    private String className;

    @NotBlank
    private String academicYear;

    @NotNull
    private UUID majorId;

    private String educationType;
    private String trainingLevel;

    @Min(1)
    private Integer maxStudent;

    private String classStatus;
    private Boolean isActive;
}

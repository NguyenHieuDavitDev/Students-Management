package com.example.stduents_management.courseregistration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CourseRegistrationRequest {

    @NotNull(message = "Sinh viên không được để trống")
    private UUID studentId;

    @NotNull(message = "Lớp học phần không được để trống")
    private Long classSectionId;

    private String note;
}


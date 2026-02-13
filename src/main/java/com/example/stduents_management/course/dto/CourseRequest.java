package com.example.stduents_management.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CourseRequest {

    @NotBlank(message = "Mã học phần không được để trống")
    @Size(max = 50)
    private String courseCode;

    @NotBlank(message = "Tên học phần không được để trống")
    @Size(max = 200)
    private String courseName;

    private Integer credits;
    private Integer lectureHours;
    private Integer practiceHours;

    @NotNull(message = "Vui lòng chọn khoa")
    private UUID facultyId;

    @Size(max = 1000)
    private String description;

    private Boolean status = true;
}
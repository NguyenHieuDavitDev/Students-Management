package com.example.stduents_management.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CourseRequest {

    @NotBlank(message = "Mã học phần không được để trống")
    @Size(max = 50, message = "Mã học phần tối đa 50 ký tự")
    private String courseCode;

    @NotBlank(message = "Tên học phần không được để trống")
    @Size(max = 200, message = "Tên học phần tối đa 200 ký tự")
    private String courseName;

    @NotNull(message = "Số tín chỉ không được để trống")
    @Min(value = 1, message = "Số tín chỉ phải lớn hơn hoặc bằng 1")
    @Max(value = 10, message = "Số tín chỉ không được vượt quá 10")
    private Integer credits;

    @NotNull(message = "Số giờ lý thuyết không được để trống")
    @Min(value = 0, message = "Số giờ lý thuyết không được âm")
    private Integer lectureHours = 0;

    @NotNull(message = "Số giờ thực hành không được để trống")
    @Min(value = 0, message = "Số giờ thực hành không được âm")
    private Integer practiceHours = 0;

    @NotNull(message = "Vui lòng chọn khoa")
    private UUID facultyId;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    private String description;

    @NotNull
    private Boolean status = true;
}
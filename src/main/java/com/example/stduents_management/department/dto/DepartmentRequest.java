package com.example.stduents_management.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Mã phòng ban không được để trống")
    @Size(max = 30)
    private String departmentCode;

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 150)
    private String departmentName;

    @Size(max = 500)
    private String description;
}

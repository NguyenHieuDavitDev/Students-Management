package com.example.stduents_management.educationtype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EducationTypeRequest {

    @NotBlank(message = "Tên hệ đào tạo không được để trống")
    @Size(max = 100, message = "Tối đa 100 ký tự")
    private String educationTypeName;

    private Boolean isActive = true;
}

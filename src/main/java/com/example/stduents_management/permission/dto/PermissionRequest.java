package com.example.stduents_management.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionRequest {

    @NotBlank(message = "Mã quyền không được để trống")
    @Size(max = 80)
    private String code;

    @NotBlank(message = "Tên quyền không được để trống")
    @Size(max = 150)
    private String name;

    @Size(max = 255)
    private String description;
}

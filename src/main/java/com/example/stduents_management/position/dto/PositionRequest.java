package com.example.stduents_management.position.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PositionRequest {

    @NotBlank(message = "Mã chức danh không được để trống")
    @Size(max = 20)
    private String positionCode;

    @NotBlank(message = "Tên chức danh không được để trống")
    @Size(max = 150)
    private String positionName;

    @Size(max = 500)
    private String description;
}

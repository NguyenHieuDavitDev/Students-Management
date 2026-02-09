package com.example.stduents_management.traininglevel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TrainingLevelRequest {

    @NotBlank(message = "Tên trình độ không được để trống")
    @Size(max = 100, message = "Tên trình độ tối đa 100 ký tự")
    private String trainingLevelName;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;
}

package com.example.stduents_management.trainingprogram.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class TrainingProgramRequest {

    @NotBlank(message = "Mã chương trình không được để trống")
    @Size(max = 50, message = "Mã chương trình không được vượt quá 50 ký tự")
    private String programCode;

    @NotBlank(message = "Tên chương trình không được để trống")
    @Size(max = 200, message = "Tên chương trình không được vượt quá 200 ký tự")
    private String programName;

    @NotNull(message = "Vui lòng chọn ngành")
    private UUID majorId;

    @NotBlank(message = "Khóa học không được để trống")
    @Size(max = 20, message = "Khóa học không được vượt quá 20 ký tự")
    private String course;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    private Integer durationYears;

    private Integer totalCredits;

    private Boolean isActive = true;
}

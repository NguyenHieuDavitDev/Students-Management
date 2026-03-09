package com.example.stduents_management.gradecomponent.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeComponentRequest {

    @NotNull(message = "Lớp học phần không được để trống")
    private Long courseClassId;

    @NotBlank(message = "Tên thành phần điểm không được để trống")
    @Size(max = 100)
    private String componentName;

    @DecimalMin(value = "0", message = "Trọng số phải >= 0")
    @DecimalMax(value = "100", message = "Trọng số phải <= 100")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal weight;

    @DecimalMin(value = "0", message = "Điểm tối đa phải >= 0")
    @Digits(integer = 2, fraction = 2)
    private BigDecimal maxScore;
}

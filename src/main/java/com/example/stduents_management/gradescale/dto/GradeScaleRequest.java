package com.example.stduents_management.gradescale.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeScaleRequest {

    @NotNull(message = "Điểm tối thiểu không được để trống")
    @DecimalMin(value = "0", message = "Điểm tối thiểu phải >= 0")
    @DecimalMax(value = "100", message = "Điểm tối thiểu phải <= 100")
    @Digits(integer = 2, fraction = 2)
    private BigDecimal minScore;

    @NotNull(message = "Điểm tối đa không được để trống")
    @DecimalMin(value = "0", message = "Điểm tối đa phải >= 0")
    @DecimalMax(value = "100", message = "Điểm tối đa phải <= 100")
    @Digits(integer = 2, fraction = 2)
    private BigDecimal maxScore;

    @NotBlank(message = "Điểm chữ không được để trống")
    @Size(max = 2, message = "Điểm chữ tối đa 2 ký tự")
    private String letterGrade;

    @NotNull(message = "Điểm GPA không được để trống")
    @DecimalMin(value = "0", message = "GPA phải >= 0")
    @DecimalMax(value = "4", message = "GPA phải <= 4")
    @Digits(integer = 1, fraction = 2)
    private BigDecimal gradePoint;

    @Size(max = 100)
    private String description;
}

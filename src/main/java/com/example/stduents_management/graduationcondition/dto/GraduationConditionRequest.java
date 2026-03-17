package com.example.stduents_management.graduationcondition.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class GraduationConditionRequest {

    @NotNull(message = "Chương trình đào tạo không được để trống")
    private UUID programId;

    @Min(value = 0, message = "Số tín chỉ tối thiểu không được âm")
    @Max(value = 999, message = "Số tín chỉ tối thiểu không hợp lệ")
    private Integer minCredits;

    @DecimalMin(value = "0.00", message = "Điểm TB tích lũy tối thiểu từ 0.00")
    @DecimalMax(value = "4.00", message = "Điểm TB tích lũy tối đa 4.00")
    @Digits(integer = 1, fraction = 2, message = "Điểm GPA ví dụ: 2.50")
    private BigDecimal minGpa;

    @Size(max = 500, message = "Chứng chỉ bắt buộc không quá 500 ký tự")
    private String requiredCertificate;

    /** Các học phần bắt buộc (mỗi dòng một mã hoặc mô tả) */
    private String requiredCourses;
}

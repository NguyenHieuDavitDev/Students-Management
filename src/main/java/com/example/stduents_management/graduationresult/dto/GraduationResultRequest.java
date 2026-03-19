package com.example.stduents_management.graduationresult.dto;

import com.example.stduents_management.graduationresult.entity.GraduationResultStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class GraduationResultRequest {

    @NotNull(message = "Sinh viên không được để trống")
    private UUID studentId;

    @NotNull(message = "Chương trình đào tạo không được để trống")
    private UUID programId;

    @Min(value = 0, message = "Tổng tín chỉ không được âm")
    @Max(value = 999, message = "Tổng tín chỉ không hợp lệ")
    private Integer totalCredits;

    @DecimalMin(value = "0.00", message = "GPA tối thiểu từ 0.00")
    @DecimalMax(value = "4.00", message = "GPA tối đa 4.00")
    @Digits(integer = 1, fraction = 2, message = "GPA ví dụ: 3.25")
    private BigDecimal gpa;

    @Size(max = 500, message = "Chứng chỉ không quá 500 ký tự")
    private String certificates;

    private String missingCourses;

    @Size(max = 500, message = "Ghi chú không quá 500 ký tự")
    private String note;

    @NotNull(message = "Trạng thái không được để trống")
    private GraduationResultStatus status;
}


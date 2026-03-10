package com.example.stduents_management.studentgrade.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class StudentGradeRequest {

    @NotNull(message = "Sinh viên không được để trống")
    private UUID studentId;

    @NotNull(message = "Lớp học phần không được để trống")
    private Long courseClassId;

    @NotNull(message = "Thành phần điểm không được để trống")
    private UUID gradeComponentId;

    @DecimalMin(value = "0", message = "Điểm phải >= 0")
    @DecimalMax(value = "100", message = "Điểm phải <= 100")
    @Digits(integer = 2, fraction = 2)
    private BigDecimal score;

    /** Giảng viên nhập điểm (bắt buộc khi Admin nhập; tự động nếu đăng nhập là giảng viên) */
    private UUID gradedByLecturerId;
}

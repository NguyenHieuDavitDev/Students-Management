package com.example.stduents_management.semester.dto;

import com.example.stduents_management.semester.entity.SemesterStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterRequest {

    @NotBlank(message = "Mã học kỳ không được để trống")
    @Size(max = 20)
    private String code;

    @NotBlank(message = "Tên học kỳ không được để trống")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Năm học không được để trống")
    @Size(max = 20)
    private String academicYear;

    @NotNull(message = "Kỳ không được để trống")
    @Min(value = 1, message = "Kỳ phải là 1, 2 hoặc 3")
    @Max(value = 3, message = "Kỳ phải là 1, 2 hoặc 3")
    private Integer term;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @NotNull(message = "Ngày mở đăng ký không được để trống")
    private LocalDate registrationStart;

    @NotNull(message = "Ngày đóng đăng ký không được để trống")
    private LocalDate registrationEnd;

    @NotNull(message = "Trạng thái không được để trống")
    private SemesterStatus status;

    @Size(max = 500)
    private String description;

    @AssertTrue(message = "Ngày bắt đầu phải nhỏ hơn ngày kết thúc")
    public boolean isValidStartEnd() {
        return startDate == null || endDate == null || startDate.isBefore(endDate);
    }

    @AssertTrue(message = "Ngày mở đăng ký phải nhỏ hơn hoặc bằng ngày đóng đăng ký")
    public boolean isValidRegistrationRange() {
        return registrationStart == null || registrationEnd == null
                || !registrationStart.isAfter(registrationEnd);
    }

    @AssertTrue(message = "Ngày đóng đăng ký phải nhỏ hơn hoặc bằng ngày bắt đầu học kỳ")
    public boolean isValidRegistrationEnd() {
        return registrationEnd == null || startDate == null
                || !registrationEnd.isAfter(startDate);
    }
}
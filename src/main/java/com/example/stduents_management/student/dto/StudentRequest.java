package com.example.stduents_management.student.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class StudentRequest {

    @NotBlank(message = "Mã sinh viên không được để trống")
    private String studentCode;

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Past(message = "Ngày sinh không hợp lệ")
    private LocalDate dateOfBirth;

    private String gender;
    private String citizenId;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String phoneNumber;
    private String address;
    private String avatar;

    @NotNull(message = "Vui lòng chọn lớp")
    private UUID classId;
}

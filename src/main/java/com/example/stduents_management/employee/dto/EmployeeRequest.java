package com.example.stduents_management.employee.dto;

import com.example.stduents_management.employee.entity.EmployeeType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class EmployeeRequest {

    @NotBlank(message = "Mã nhân sự không được để trống")
    @Size(max = 30)
    private String employeeCode;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150)
    private String fullName;

    @Past(message = "Ngày sinh không hợp lệ")
    private LocalDate dateOfBirth;

    @Size(max = 10)
    private String gender;

    @Size(max = 20)
    private String citizenId;

    @Email(message = "Email không hợp lệ")
    @Size(max = 150)
    private String email;

    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 255)
    private String address;

    private MultipartFile avatarFile;
    private String avatar;

    @NotNull(message = "Loại nhân sự không được để trống")
    private EmployeeType employeeType;

    private UUID positionId;
    private UUID departmentId;

    /** Bắt buộc khi tạo mới loại LECTURER (hoặc khi chưa có hồ sơ GV và cần gắn khoa). */
    private UUID facultyId;

    /** Số quyết định / ghi chú khi ghi nhận thay đổi phòng ban–chức danh–loại NS. */
    @Size(max = 100)
    private String decisionNo;

    @Size(max = 20)
    private String status; // ACTIVE/INACTIVE
}


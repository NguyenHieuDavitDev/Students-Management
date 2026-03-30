package com.example.stduents_management.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 100)
    private String username;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    // dùng cho đăng nhập
    private String password;

    private boolean enabled = true;

    private Set<UUID> roleIds;

    /** ID sinh viên gắn với tài khoản (1-1). Null = không gắn. */
    private UUID studentId;
    /** ID giảng viên gắn với tài khoản (1-1). Null = không gắn. */
    private UUID lecturerId;

    @AssertTrue(message = "Password must be at least 6 characters")
    public boolean isPasswordValidForCreateOrUpdate() {
        return password == null || password.isBlank() || password.length() >= 6;
    }
}

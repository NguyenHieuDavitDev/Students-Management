package com.example.stduents_management.user.dto;

import jakarta.validation.constraints.Email;
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
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private boolean enabled = true;

    private Set<UUID> roleIds;
}

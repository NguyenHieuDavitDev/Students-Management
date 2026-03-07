package com.example.stduents_management.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;
    private UUID studentId;   // null nếu không gắn sinh viên
    private UUID lecturerId;  // null nếu không gắn giảng viên
}

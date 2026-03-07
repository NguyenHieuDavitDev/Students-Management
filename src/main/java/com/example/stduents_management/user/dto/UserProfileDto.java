package com.example.stduents_management.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO hiển thị profile user ở header/footer: tên hiển thị, avatar, loại tài khoản (sinh viên/giảng viên/admin).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private String username;
    private String displayName;
    private String avatar;       // URL hoặc path ảnh đại diện
    private String roleLabel;    // "Sinh viên", "Giảng viên", "Quản trị viên", ...
    private boolean student;     // true nếu user gắn với bảng students
    private boolean lecturer;    // true nếu user gắn với bảng lecturers
    private boolean canAccessDashboard; // true nếu có quyền vào /admin
    /** Chỉ ADMIN mới true: được xem và thao tác Roles, Users, Permissions. */
    private boolean canManageRolesUsersPermissions;
}

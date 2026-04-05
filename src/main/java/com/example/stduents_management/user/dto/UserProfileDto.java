package com.example.stduents_management.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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

    /** ADMIN: luôn hiển thị toàn bộ mục sidebar. */
    @Builder.Default
    private boolean sidebarMenuUnrestricted = false;
    /**
     * true khi user (không phải ADMIN) có ít nhất một quyền được gán cho vai trò của họ gắn với mục sidebar —
     * khi đó chỉ hiện các mục có trong {@link #visibleSidebarMenuKeys} (+ Overview / Thông báo).
     */
    @Builder.Default
    private boolean sidebarMenuRestricted = false;
    @Builder.Default
    private Set<String> visibleSidebarMenuKeys = Set.of();
    /** Tiêu đề nhóm sidebar cần hiện khi {@link #sidebarMenuRestricted}. */
    @Builder.Default
    private Set<String> visibleSidebarSectionIds = Set.of();

    /** Mục sidebar theo {@code activeMenu} (trừ overview / notifications luôn dùng {@link #canAccessDashboard}). */
    public boolean canShowSidebarMenuItem(String menuKey) {
        if (sidebarMenuUnrestricted || !sidebarMenuRestricted) {
            return true;
        }
        return visibleSidebarMenuKeys != null && menuKey != null && visibleSidebarMenuKeys.contains(menuKey);
    }

    public boolean canShowSidebarSection(String sectionId) {
        if (sidebarMenuUnrestricted || !sidebarMenuRestricted) {
            return true;
        }
        return visibleSidebarSectionIds != null && sectionId != null && visibleSidebarSectionIds.contains(sectionId);
    }
}

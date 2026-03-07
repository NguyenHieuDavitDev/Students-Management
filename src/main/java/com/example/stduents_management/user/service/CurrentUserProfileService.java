package com.example.stduents_management.user.service;

import com.example.stduents_management.role.entity.Role;
import com.example.stduents_management.user.dto.UserProfileDto;
import com.example.stduents_management.user.entity.User;
import com.example.stduents_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lấy thông tin profile của user đang đăng nhập (để hiển thị header, sidebar).
 * Phân biệt user là sinh viên (gắn Student) hay giảng viên (gắn Lecturer) hay admin.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserProfileService {

    private final UserRepository userRepository;

    /**
     * Trả về profile của user đang đăng nhập, hoặc empty nếu chưa đăng nhập.
     */
    @Transactional(readOnly = true)
    public Optional<UserProfileDto> getCurrentProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String username = auth.getName();
        return userRepository.findByUsernameWithProfile(username).map(this::toProfileDto);
    }

    private UserProfileDto toProfileDto(User user) {
        String displayName = user.getUsername();
        String avatar = null;
        boolean isStudent = user.getStudent() != null;
        boolean isLecturer = user.getLecturer() != null;

        if (isStudent) {
            displayName = user.getStudent().getFullName();
            if (user.getStudent().getAvatar() != null && !user.getStudent().getAvatar().isBlank()) {
                avatar = user.getStudent().getAvatar();
            }
        } else if (isLecturer) {
            displayName = user.getLecturer().getFullName();
            if (user.getLecturer().getAvatar() != null && !user.getLecturer().getAvatar().isBlank()) {
                avatar = user.getLecturer().getAvatar();
            }
        }

        String roleLabel = toRoleLabel(user.getRoles(), isStudent, isLecturer);
        boolean canAccessDashboard = user.getRoles().stream()
                .anyMatch(r -> Set.of("ADMIN", "MANAGER", "LECTURER").contains(r.getName().toUpperCase()));
        boolean canManageRolesUsersPermissions = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()));

        return UserProfileDto.builder()
                .username(user.getUsername())
                .displayName(displayName)
                .avatar(avatar)
                .roleLabel(roleLabel)
                .student(isStudent)
                .lecturer(isLecturer)
                .canAccessDashboard(canAccessDashboard)
                .canManageRolesUsersPermissions(canManageRolesUsersPermissions)
                .build();
    }

    private String toRoleLabel(Set<Role> roles, boolean isStudent, boolean isLecturer) {
        if (roles == null || roles.isEmpty()) {
            return isStudent ? "Sinh viên" : isLecturer ? "Giảng viên" : "Người dùng";
        }
        Set<String> names = roles.stream().map(r -> r.getName().toUpperCase()).collect(Collectors.toSet());
        if (names.contains("ADMIN")) return "Quản trị viên";
        if (names.contains("MANAGER")) return "Quản lý";
        if (names.contains("LECTURER") || isLecturer) return "Giảng viên";
        if (names.contains("STUDENT") || isStudent) return "Sinh viên";
        return roles.stream().map(Role::getName).findFirst().orElse("Người dùng");
    }
}

package com.example.stduents_management.permission.service;

import com.example.stduents_management.permission.SidebarMenuDefinition;
import com.example.stduents_management.permission.entity.Permission;
import com.example.stduents_management.permission.entity.RolePermission;
import com.example.stduents_management.permission.repository.PermissionRepository;
import com.example.stduents_management.permission.repository.RolePermissionRepository;
import com.example.stduents_management.role.entity.Role;
import com.example.stduents_management.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cấu hình nhanh menu sidebar theo vai trò: đồng bộ qua bảng gán quyền (quyền có {@code sidebarMenuKey}).
 */
@Service
@RequiredArgsConstructor
public class RoleSidebarMenuService {

    private static final String AUTO_MENU_CODE_PREFIX = "AUTO_MENU_";

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional(readOnly = true)
    public Set<String> getMenuKeysForRole(UUID roleId) {
        return rolePermissionRepository.findDistinctSidebarMenuKeysByRoleIds(List.of(roleId));
    }

    /**
     * Bỏ mọi gán quyền có {@code sidebarMenuKey} trùng mục bị bỏ chọn; thêm quyền hệ thống {@code AUTO_MENU_*} nếu cần cho mục được chọn.
     */
    @Transactional
    public void syncRoleSidebarMenus(UUID roleId, Set<String> selectedMenuKeys) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vai trò"));

        Set<String> allKeys = Arrays.stream(SidebarMenuDefinition.values())
                .map(SidebarMenuDefinition::getMenuKey)
                .collect(Collectors.toSet());

        Set<String> wanted = selectedMenuKeys.stream()
                .filter(allKeys::contains)
                .collect(Collectors.toSet());

        for (String key : allKeys) {
            if (!wanted.contains(key)) {
                rolePermissionRepository.deleteByRoleIdAndPermissionSidebarMenuKey(roleId, key);
            }
        }

        for (String key : wanted) {
            if (rolePermissionRepository.countByRole_IdAndPermission_SidebarMenuKey(roleId, key) > 0) {
                continue;
            }
            Permission p = ensureAutoMenuPermission(key);
            if (!rolePermissionRepository.existsByRole_IdAndPermission_Id(roleId, p.getId())) {
                RolePermission rp = new RolePermission();
                rp.setRole(role);
                rp.setPermission(p);
                rolePermissionRepository.save(rp);
            }
        }
    }

    private Permission ensureAutoMenuPermission(String menuKey) {
        String code = canonicalAutoMenuCode(menuKey);
        return permissionRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
            SidebarMenuDefinition def = SidebarMenuDefinition.fromMenuKey(menuKey)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mục menu không hợp lệ"));
            Permission p = new Permission();
            p.setCode(code);
            p.setName("Hiển thị menu: " + def.getLabelVi());
            p.setDescription("Quyền tự tạo khi cấu hình menu sidebar theo vai trò (Quản lý quyền).");
            p.setSidebarMenuKey(menuKey);
            return permissionRepository.save(p);
        });
    }

    static String canonicalAutoMenuCode(String menuKey) {
        String suffix = menuKey.trim().replace('-', '_').toUpperCase();
        return AUTO_MENU_CODE_PREFIX + suffix;
    }
}

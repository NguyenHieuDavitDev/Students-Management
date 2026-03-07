package com.example.stduents_management.permission.dto;

import java.util.UUID;

public record RolePermissionResponse(
        UUID id,
        UUID roleId,
        String roleName,
        UUID permissionId,
        String permissionCode,
        String permissionName
) {}

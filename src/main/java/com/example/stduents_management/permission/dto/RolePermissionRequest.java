package com.example.stduents_management.permission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RolePermissionRequest {

    @NotNull(message = "Vai trò không được để trống")
    private UUID roleId;

    @NotNull(message = "Quyền không được để trống")
    private UUID permissionId;
}

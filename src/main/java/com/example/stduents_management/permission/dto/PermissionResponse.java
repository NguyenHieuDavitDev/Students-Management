package com.example.stduents_management.permission.dto;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        String code,
        String name,
        String description,
        String sidebarMenuKey,
        String sidebarMenuLabelVi
) {}

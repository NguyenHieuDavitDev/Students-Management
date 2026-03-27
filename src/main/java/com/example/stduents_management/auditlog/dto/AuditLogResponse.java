package com.example.stduents_management.auditlog.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String action,
        String moduleName,
        String targetId,
        String description,
        String username,
        String httpMethod,
        String requestPath,
        String ipAddress,
        String userAgent,
        Integer statusCode,
        LocalDateTime createdAt
) {
}

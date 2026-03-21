package com.example.stduents_management.notification.dto;

import com.example.stduents_management.notification.entity.NotificationCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientUserId,
        String recipientUsername,
        NotificationCategory category,
        String categoryLabel,
        String title,
        String content,
        LocalDateTime createdAt,
        boolean read,
        LocalDateTime readAt,
        LocalDateTime scheduledAt
) {
}


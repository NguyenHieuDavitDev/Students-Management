package com.example.stduents_management.notification.dto;

import com.example.stduents_management.notification.entity.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class NotificationCreateRequest {

    @NotNull(message = "Danh mục thông báo không được để trống")
    private NotificationCategory category;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @NotNull(message = "Người nhận không được để trống")
    private UUID recipientUserId;

    private LocalDateTime scheduledAt;
}


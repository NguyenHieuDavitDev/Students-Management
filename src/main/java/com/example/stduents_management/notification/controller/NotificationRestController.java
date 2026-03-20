package com.example.stduents_management.notification.controller;

import com.example.stduents_management.notification.dto.NotificationCreateRequest;
import com.example.stduents_management.notification.dto.NotificationResponse;
import com.example.stduents_management.notification.entity.NotificationCategory;
import com.example.stduents_management.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;

    // ─── User: xem thông báo của chính mình ─────────────────────────────────────
    @GetMapping("/api/me/notifications")
    public ResponseEntity<Page<NotificationResponse>> searchForMe(
            @RequestParam(required = false) NotificationCategory category,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                notificationService.searchForCurrentUser(category, unreadOnly, PageRequest.of(page, size))
        );
    }

    @PutMapping("/api/me/notifications/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    // ─── Admin/System: tạo thông báo cho user bất kỳ ───────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/notifications")
    public ResponseEntity<NotificationResponse> createForUser(
            @Valid @RequestBody NotificationCreateRequest req
    ) {
        NotificationResponse created = notificationService.createForUserId(
                req.getRecipientUserId(),
                req.getCategory(),
                req.getTitle(),
                req.getContent(),
                req.getScheduledAt(),
                null
        );
        return ResponseEntity.ok(created);
    }
}


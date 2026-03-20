package com.example.stduents_management.notification.service;

import com.example.stduents_management.notification.dto.NotificationResponse;
import com.example.stduents_management.notification.entity.Notification;
import com.example.stduents_management.notification.entity.NotificationCategory;
import com.example.stduents_management.notification.repository.NotificationRepository;
import com.example.stduents_management.user.entity.User;
import com.example.stduents_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy tài khoản"));
        return user.getId();
    }

    @Transactional
    public NotificationResponse createForUserId(
            UUID recipientUserId,
            NotificationCategory category,
            String title,
            String content,
            LocalDateTime scheduledAt,
            UUID createdBy
    ) {
        if (recipientUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recipientUserId không hợp lệ");
        }
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category không hợp lệ");
        }

        // getReferenceById để tránh query thừa; sẽ lỗi nếu id không tồn tại.
        User recipient = userRepository.getReferenceById(recipientUserId);

        Notification notification = new Notification();
        notification.setRecipientUser(recipient);
        notification.setCategory(category);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setScheduledAt(scheduledAt);
        notification.setCreatedBy(createdBy);

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    public NotificationResponse createForUserId(
            UUID recipientUserId,
            NotificationCategory category,
            String title,
            String content
    ) {
        return createForUserId(recipientUserId, category, title, content, null, null);
    }

    public Page<NotificationResponse> searchForCurrentUser(
            NotificationCategory category,
            boolean unreadOnly,
            Pageable pageable
    ) {
        UUID userId = getCurrentUserId();
        return notificationRepository
                .searchForUser(userId, category, unreadOnly, pageable)
                .map(this::toResponse);
    }

    public long countUnreadForCurrentUser() {
        UUID userId = getCurrentUserId();
        return notificationRepository.countByRecipientUser_IdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        if (notificationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "notificationId không hợp lệ");
        }
        UUID userId = getCurrentUserId();
        Notification notification = notificationRepository.findByIdAndRecipientUser_Id(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getCategory(),
                n.getCategory() != null ? n.getCategory().getLabel() : null,
                n.getTitle(),
                n.getContent(),
                n.getCreatedAt(),
                n.isRead(),
                n.getReadAt(),
                n.getScheduledAt()
        );
    }
}


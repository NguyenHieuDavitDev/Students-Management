package com.example.stduents_management.notification.service;

import com.example.stduents_management.notification.dto.NotificationResponse;
import com.example.stduents_management.notification.dto.NotificationCreateRequest;
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

    public Page<NotificationResponse> searchForAdmin(
            NotificationCategory category,
            UUID recipientUserId,
            boolean unreadOnly,
            String keyword,
            Pageable pageable
    ) {
        return notificationRepository.searchForAdmin(category, recipientUserId, unreadOnly, keyword, pageable)
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

    public NotificationResponse getById(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));
    }

    @Transactional
    public NotificationResponse createManual(NotificationCreateRequest req) {
        return createForUserId(
                req.getRecipientUserId(),
                req.getCategory(),
                req.getTitle(),
                req.getContent(),
                req.getScheduledAt(),
                null
        );
    }

    @Transactional
    public NotificationResponse updateManual(UUID notificationId, NotificationCreateRequest req) {
        Notification existing = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo"));

        User recipient = userRepository.getReferenceById(req.getRecipientUserId());
        existing.setRecipientUser(recipient);
        existing.setCategory(req.getCategory());
        existing.setTitle(req.getTitle());
        existing.setContent(req.getContent());
        existing.setScheduledAt(req.getScheduledAt());

        // Thông báo tạo tay: không gắn nguồn sự kiện để tránh bị auto CRUD ghi đè.
        existing.setSourceType(null);
        existing.setSourceId(null);

        Notification saved = notificationRepository.save(existing);
        return toResponse(saved);
    }

    @Transactional
    public void deleteManual(UUID notificationId) {
        if (notificationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "notificationId không hợp lệ");
        }
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông báo");
        }
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Auto CRUD theo nguồn sự kiện: nếu đã có notification cho cùng user + category + sourceType + sourceId
     * thì update title/content/scheduledAt; ngược lại thì tạo mới.
     *
     * Lưu ý: phần read/readAt không bị reset khi auto update.
     */
    @Transactional
    public NotificationResponse upsertForUserBySource(
            UUID recipientUserId,
            NotificationCategory category,
            String title,
            String content,
            LocalDateTime scheduledAt,
            String sourceType,
            String sourceId
    ) {
        if (recipientUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "recipientUserId không hợp lệ");
        }
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "category không hợp lệ");
        }
        if (sourceType == null || sourceType.isBlank() || sourceId == null || sourceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceType/sourceId không hợp lệ");
        }

        User recipient = userRepository.getReferenceById(recipientUserId);

        Notification existing = notificationRepository.findLatestBySource(recipientUserId, category, sourceType, sourceId)
                .orElse(null);
        if (existing != null) {
            existing.setRecipientUser(recipient);
            existing.setTitle(title);
            existing.setContent(content);
            existing.setCategory(category);
            existing.setSourceType(sourceType);
            existing.setSourceId(sourceId);
            if (scheduledAt != null) {
                existing.setScheduledAt(scheduledAt);
            }
            // giữ nguyên read/readAt
            Notification saved = notificationRepository.save(existing);
            return toResponse(saved);
        }

        Notification created = new Notification();
        created.setRecipientUser(recipient);
        created.setCategory(category);
        created.setTitle(title);
        created.setContent(content);
        created.setSourceType(sourceType);
        created.setSourceId(sourceId);
        if (scheduledAt != null) {
            created.setScheduledAt(scheduledAt);
        }
        // read/readAt mặc định: false/null

        Notification saved = notificationRepository.save(created);
        return toResponse(saved);
    }

    @Transactional
    public void deleteBySource(String sourceType, String sourceId) {
        if (sourceType == null || sourceType.isBlank() || sourceId == null || sourceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sourceType/sourceId không hợp lệ");
        }
        notificationRepository.deleteBySourceTypeAndSourceId(sourceType, sourceId);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getRecipientUser() != null ? n.getRecipientUser().getId() : null,
                n.getRecipientUser() != null ? n.getRecipientUser().getUsername() : null,
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


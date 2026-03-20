package com.example.stduents_management.notification.entity;

import com.example.stduents_management.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id",
            columnDefinition = "uniqueidentifier",
            nullable = false,
            updatable = false
    )
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "recipient_user_id",
            nullable = false,
            columnDefinition = "uniqueidentifier"
    )
    private User recipientUser;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            length = 50,
            nullable = false,
            columnDefinition = "VARCHAR(50)"
    )
    private NotificationCategory category;

    @Column(name = "title", nullable = false, columnDefinition = "NVARCHAR(200)")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(2000)")
    private String content;

    // read = true khi người dùng đã xem thông báo
    @Column(
            name = "is_read",
            nullable = false,
            columnDefinition = "BIT DEFAULT 0"
    )
    private boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    // dùng để truy vết ai tạo thông báo (admin/system). null nếu không áp dụng.
    @Column(name = "created_by", columnDefinition = "uniqueidentifier")
    private UUID createdBy;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (title != null) {
            title = title.trim();
        }
        if (content != null) {
            content = content.trim();
        }
    }
}


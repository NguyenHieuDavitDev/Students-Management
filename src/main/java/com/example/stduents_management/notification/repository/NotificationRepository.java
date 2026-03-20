package com.example.stduents_management.notification.repository;

import com.example.stduents_management.notification.entity.Notification;
import com.example.stduents_management.notification.entity.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("""
            select n from Notification n
            where n.recipientUser.id = :recipientUserId
              and (:category is null or n.category = :category)
              and (:unreadOnly = false or n.read = false)
            order by n.createdAt desc
            """)
    Page<Notification> searchForUser(
            @Param("recipientUserId") UUID recipientUserId,
            @Param("category") NotificationCategory category,
            @Param("unreadOnly") boolean unreadOnly,
            Pageable pageable
    );

    Optional<Notification> findByIdAndRecipientUser_Id(UUID id, UUID recipientUserId);

    long countByRecipientUser_IdAndReadFalse(UUID recipientUserId);
}


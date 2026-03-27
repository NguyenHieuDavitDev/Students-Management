package com.example.stduents_management.auditlog.repository;

import com.example.stduents_management.auditlog.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:keyword IS NULL OR :keyword = '' OR
                   LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(a.requestPath) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(a.targetId) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:action IS NULL OR :action = '' OR a.action = :action)
              AND (:moduleName IS NULL OR :moduleName = '' OR LOWER(a.moduleName) LIKE LOWER(CONCAT('%', :moduleName, '%')))
              AND (:fromAt IS NULL OR a.createdAt >= :fromAt)
              AND (:toAt IS NULL OR a.createdAt <= :toAt)
            """)
    Page<AuditLog> search(
            @Param("keyword") String keyword,
            @Param("action") String action,
            @Param("moduleName") String moduleName,
            @Param("fromAt") LocalDateTime fromAt,
            @Param("toAt") LocalDateTime toAt,
            Pageable pageable
    );

    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
}

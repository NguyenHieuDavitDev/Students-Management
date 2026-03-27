package com.example.stduents_management.auditlog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
                @Index(name = "idx_audit_logs_action", columnList = "action"),
                @Index(name = "idx_audit_logs_module", columnList = "module_name"),
                @Index(name = "idx_audit_logs_username", columnList = "username")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String action;

    @Column(name = "module_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String moduleName;

    @Column(name = "target_id", length = 100, columnDefinition = "NVARCHAR(100)")
    private String targetId;

    @Column(length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String username;

    @Column(name = "http_method", length = 10, columnDefinition = "VARCHAR(10)")
    private String httpMethod;

    @Column(name = "request_path", length = 300, columnDefinition = "NVARCHAR(300)")
    private String requestPath;

    @Column(name = "ip_address", length = 64, columnDefinition = "VARCHAR(64)")
    private String ipAddress;

    @Column(name = "user_agent", length = 255, columnDefinition = "NVARCHAR(255)")
    private String userAgent;

    @Column(name = "status_code")
    private Integer statusCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

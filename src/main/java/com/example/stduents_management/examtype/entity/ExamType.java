package com.example.stduents_management.examtype.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng loại kỳ thi học phần (exam_types).
 * Chuẩn hóa các loại kỳ thi: Giữa kỳ, Cuối kỳ, Thi lại, Cải thiện...
 * Một học phần có thể có nhiều loại kỳ thi khác nhau.
 * Cột tiếng Việt dùng NVARCHAR để lưu Unicode đúng.
 */
@Entity
@Table(name = "exam_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String name;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

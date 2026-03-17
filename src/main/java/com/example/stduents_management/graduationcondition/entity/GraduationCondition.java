package com.example.stduents_management.graduationcondition.entity;

import com.example.stduents_management.trainingprogram.entity.TrainingProgram;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng điều kiện xét tốt nghiệp (graduation_conditions).
 * Lưu quy định xét tốt nghiệp của từng chương trình đào tạo.
 */
@Entity
@Table(name = "graduation_conditions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraduationCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** Chương trình đào tạo áp dụng điều kiện này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "program_id",
            nullable = false,
            columnDefinition = "uniqueidentifier"
    )
    private TrainingProgram program;

    /** Số tín chỉ tối thiểu cần tích lũy */
    @Column(name = "min_credits", columnDefinition = "INT")
    private Integer minCredits;

    /** Điểm trung bình tích lũy tối thiểu */
    @Column(name = "min_gpa", precision = 4, scale = 2, columnDefinition = "DECIMAL(4,2)")
    private BigDecimal minGpa;

    /** Chứng chỉ bắt buộc (ví dụ: ngoại ngữ, tin học) */
    @Column(name = "required_certificate", length = 500, columnDefinition = "NVARCHAR(500)")
    private String requiredCertificate;

    /** Các học phần bắt buộc phải hoàn thành (TEXT) */
    @Lob
    @Column(name = "required_courses", columnDefinition = "NVARCHAR(MAX)")
    private String requiredCourses;

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

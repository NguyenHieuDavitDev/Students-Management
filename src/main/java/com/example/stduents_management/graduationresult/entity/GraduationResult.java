package com.example.stduents_management.graduationresult.entity;

import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.trainingprogram.entity.TrainingProgram;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bảng kết quả xét tốt nghiệp (graduation_results).
 * Lưu kết quả xét theo từng sinh viên và chương trình đào tạo.
 */
@Entity
@Table(
        name = "graduation_results",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "program_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraduationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false, columnDefinition = "uniqueidentifier")
    private TrainingProgram program;

    /** Tổng tín chỉ đã tích lũy (snapshot tại thời điểm xét) */
    @Column(name = "total_credits", columnDefinition = "INT")
    private Integer totalCredits;

    /** GPA tích lũy (snapshot tại thời điểm xét) */
    @Column(name = "gpa", precision = 4, scale = 2, columnDefinition = "DECIMAL(4,2)")
    private BigDecimal gpa;

    /** Chứng chỉ sinh viên đã có (liệt kê ngắn gọn) */
    @Column(name = "certificates", length = 500, columnDefinition = "NVARCHAR(500)")
    private String certificates;

    /** Các học phần còn thiếu / chưa hoàn thành (text) */
    @Lob
    @Column(name = "missing_courses", columnDefinition = "NVARCHAR(MAX)")
    private String missingCourses;

    /** Ghi chú của quản trị viên */
    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    private GraduationResultStatus status = GraduationResultStatus.PENDING;

    /** Thời điểm xét / cập nhật kết quả */
    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (checkedAt == null) checkedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
        checkedAt = LocalDateTime.now();
    }
}


package com.example.stduents_management.examschedule.entity;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.examtype.entity.ExamType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Bảng lưu lịch thi (exam_schedules).
 * Mỗi bản ghi: 1 lớp học phần + 1 loại kỳ thi + 1 lịch thi cụ thể.
 */
@Entity
@Table(
        name = "exam_schedules",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"class_section_id", "exam_type_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    /** Lớp học phần */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;

    /** Loại kỳ thi (Giữa kỳ, Cuối kỳ, ...) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_type_id", nullable = false)
    private ExamType examType;

    /** Ngày thi */
    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    /** Giờ bắt đầu thi */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Thời gian làm bài (phút) */
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /** Ghi chú (phòng thi, hình thức, ...) nếu cần mở rộng sau */
    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;

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
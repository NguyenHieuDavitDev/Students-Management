package com.example.stduents_management.attendance.entity;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Điểm danh sinh viên theo lớp học phần và ngày điểm danh.
 */
@Entity
@Table(
        name = "attendances",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "course_class_id", "attendance_date"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attendance_id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID attendanceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_class_id", nullable = false)
    private ClassSection courseClass;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "present", nullable = false)
    private Boolean present;

    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by", columnDefinition = "uniqueidentifier")
    private Lecturer markedBy;

    @Column(name = "marked_at")
    private LocalDateTime markedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (markedAt == null) {
            markedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}


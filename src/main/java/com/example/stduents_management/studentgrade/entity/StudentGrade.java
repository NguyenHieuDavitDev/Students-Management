package com.example.stduents_management.studentgrade.entity;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.gradecomponent.entity.GradeComponent;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng điểm chi tiết của sinh viên theo từng thành phần điểm.
 * Lưu điểm đạt được cho mỗi cặp (sinh viên, lớp học phần, thành phần điểm).
 */
@Entity
@Table(
        name = "student_grades",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "course_class_id", "grade_component_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_class_id", nullable = false)
    private ClassSection courseClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_component_id", nullable = false, columnDefinition = "uniqueidentifier")
    private GradeComponent gradeComponent;

    @Column(name = "score", precision = 4, scale = 2)
    private BigDecimal score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "graded_by", columnDefinition = "uniqueidentifier")
    private Lecturer gradedBy;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
        if (this.gradedAt == null) {
            this.gradedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

package com.example.stduents_management.feedback.entity;

import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Phản hồi / đánh giá của sinh viên về giảng viên và môn học (bảng feedbacks).
 */
@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeachingFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "feedback_id", nullable = false, updatable = false, columnDefinition = "uniqueidentifier")
    private UUID feedbackId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Lecturer lecturer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Course subject;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "NVARCHAR(1000)")
    private String comment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

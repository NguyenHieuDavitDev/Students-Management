package com.example.stduents_management.course.entity;

import com.example.stduents_management.faculty.entity.Faculty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"course_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "course_code", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String courseCode;

    @Column(name = "course_name", nullable = false, columnDefinition = "NVARCHAR(200)")
    private String courseName;

    @Column(name = "credits", columnDefinition = "INT")
    private Integer credits;

    @Column(name = "lecture_hours", columnDefinition = "INT")
    private Integer lectureHours;

    @Column(name = "practice_hours", columnDefinition = "INT")
    private Integer practiceHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Faculty faculty;

    @Column(name = "description", columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "status", columnDefinition = "BIT")
    private Boolean status = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
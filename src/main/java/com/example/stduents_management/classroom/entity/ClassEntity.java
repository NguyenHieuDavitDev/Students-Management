package com.example.stduents_management.classroom.entity;

import com.example.stduents_management.major.entity.Major;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "classes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"class_code", "academic_year"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID classId;

    @Column(name = "class_code", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String classCode;

    @Column(name = "class_name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String className;

    @Column(name = "academic_year", nullable = false, columnDefinition = "NVARCHAR(20)")
    private String academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Major major;

    @Column(name = "education_type", columnDefinition = "NVARCHAR(50)")
    private String educationType;

    @Column(name = "training_level", columnDefinition = "NVARCHAR(50)")
    private String trainingLevel;

    @Column(name = "max_student")
    private Integer maxStudent;

    @Column(name = "class_status", columnDefinition = "NVARCHAR(50)")
    private String classStatus;

    @Column(name = "is_active")
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

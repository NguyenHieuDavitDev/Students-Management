package com.example.stduents_management.semester.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "semesters",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(
            name = "code",
            nullable = false,
            columnDefinition = "NVARCHAR(20)"
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            columnDefinition = "NVARCHAR(200)"
    )
    private String name;

    @Column(
            name = "academic_year",
            nullable = false,
            columnDefinition = "NVARCHAR(20)"
    )
    private String academicYear;

    @Column(name = "term", nullable = false)
    private Integer term; // 1,2,3

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "registration_start")
    private LocalDate registrationStart;

    @Column(name = "registration_end")
    private LocalDate registrationEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SemesterStatus status;

    @Column(
            name = "description",
            columnDefinition = "NVARCHAR(500)"
    )
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = SemesterStatus.UPCOMING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
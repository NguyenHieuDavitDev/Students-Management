package com.example.stduents_management.trainingprogram.entity;

import com.example.stduents_management.major.entity.Major;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "training_programs",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"program_code", "major_id", "course"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID programId;

    @Column(
            name = "program_code",
            nullable = false,
            columnDefinition = "NVARCHAR(50)"
    )
    private String programCode;

    @Column(
            name = "program_name",
            nullable = false,
            columnDefinition = "NVARCHAR(200)"
    )
    private String programName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "major_id",
            nullable = false,
            columnDefinition = "uniqueidentifier"
    )
    private Major major;

    @Column(
            name = "course",
            nullable = false,
            columnDefinition = "NVARCHAR(20)"
    )
    private String course; // Khóa học (ví dụ: K20, K21, K22)

    @Column(
            name = "description",
            columnDefinition = "NVARCHAR(1000)"
    )
    private String description;

    @Column(
            name = "duration_years",
            columnDefinition = "INT"
    )
    private Integer durationYears; // Thời gian đào tạo (năm)

    @Column(
            name = "total_credits",
            columnDefinition = "INT"
    )
    private Integer totalCredits; // Tổng số tín chỉ

    @Column(
            name = "is_active",
            columnDefinition = "BIT"
    )
    private Boolean isActive = true;
}

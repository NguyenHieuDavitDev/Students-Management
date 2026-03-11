package com.example.stduents_management.gradescale.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Bảng thang điểm (grade_scales): quy đổi điểm số sang điểm chữ và điểm GPA.
 * Ví dụ: 8.5–10 → A → 4.0 → Xuất sắc
 */
@Entity
@Table(name = "grade_scales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeScale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "min_score", precision = 4, scale = 2, nullable = false)
    private BigDecimal minScore;

    @Column(name = "max_score", precision = 4, scale = 2, nullable = false)
    private BigDecimal maxScore;

    @Column(name = "letter_grade", length = 2, nullable = false, columnDefinition = "VARCHAR(2)")
    private String letterGrade;

    @Column(name = "grade_point", precision = 3, scale = 2, nullable = false)
    private BigDecimal gradePoint;

    @Column(name = "description", columnDefinition = "NVARCHAR(100)")
    private String description;
}

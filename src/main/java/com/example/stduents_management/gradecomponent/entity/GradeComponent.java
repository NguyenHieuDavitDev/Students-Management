package com.example.stduents_management.gradecomponent.entity;

import com.example.stduents_management.classsection.entity.ClassSection;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Thành phần điểm (grade_components): định nghĩa các thành phần cấu thành điểm tổng kết của một lớp học phần.
 */
@Entity
@Table(name = "grade_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_class_id", nullable = false)
    private ClassSection classSection;

    @Column(name = "component_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String componentName;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "max_score", precision = 4, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

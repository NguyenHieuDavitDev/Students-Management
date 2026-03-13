package com.example.stduents_management.studenttuition.entity;

import com.example.stduents_management.semester.entity.Semester;
import com.example.stduents_management.student.entity.Student;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "student_tuition",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"student_id", "semester_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentTuition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_id",
            nullable = false,
            columnDefinition = "uniqueidentifier"
    )
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "semester_id",
            nullable = false
    )
    private Semester semester;

    @Column(name = "total_credits", nullable = false)
    private Integer totalCredits;

    @Column(name = "total_amount", precision = 18, scale = 0, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", precision = 18, scale = 0, nullable = false)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "remaining_amount", precision = 18, scale = 0, nullable = false)
    private BigDecimal remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    private StudentTuitionStatus status = StudentTuitionStatus.UNPAID;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (amountPaid == null) {
            amountPaid = BigDecimal.ZERO;
        }
        if (totalAmount != null && amountPaid != null && remainingAmount == null) {
            remainingAmount = totalAmount.subtract(amountPaid);
        }
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (totalAmount != null && amountPaid != null) {
            remainingAmount = totalAmount.subtract(amountPaid);
        }
    }
}


package com.example.stduents_management.payment.entity;

import com.example.stduents_management.studenttuition.entity.StudentTuition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bảng payments – lịch sử thanh toán học phí.
 * Mỗi lần sinh viên thanh toán (chuyển khoản, tiền mặt, ví điện tử) được ghi một bản ghi.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_tuition_id", nullable = false, columnDefinition = "uniqueidentifier")
    private StudentTuition studentTuition;

    @Column(name = "amount", precision = 18, scale = 0, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30, nullable = false, columnDefinition = "VARCHAR(30)")
    private PaymentMethod paymentMethod;

    @Column(name = "transaction_code", length = 100, columnDefinition = "VARCHAR(100)")
    private String transactionCode;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (paymentDate == null) {
            paymentDate = now;
        }
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.example.stduents_management.tuitionfee.entity;

import com.example.stduents_management.trainingprogram.entity.TrainingProgram;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng cấu hình học phí (tuition_fees).
 * Lưu mức học phí chuẩn theo từng chương trình đào tạo và ngày áp dụng.
 *
 * Ví dụ: Công nghệ thông tin – 450.000 VNĐ/tín chỉ (từ 01/09/2024)
 */
@Entity
@Table(name = "tuition_fees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TuitionFee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    /** Chương trình đào tạo áp dụng mức học phí này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "program_id",
            nullable = false,
            columnDefinition = "uniqueidentifier"
    )
    private TrainingProgram trainingProgram;

    /** Học phí mỗi tín chỉ (VNĐ) */
    @Column(name = "fee_per_credit", precision = 15, scale = 0, nullable = false)
    private BigDecimal feePerCredit;

    /** Ngày bắt đầu áp dụng mức học phí */
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    /** Trạng thái áp dụng */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false, columnDefinition = "VARCHAR(20)")
    private TuitionFeeStatus status = TuitionFeeStatus.ACTIVE;

    /** Ghi chú (lý do thay đổi, năm học áp dụng, …) */
    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;

    /** Thời điểm tạo bản ghi */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật gần nhất */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

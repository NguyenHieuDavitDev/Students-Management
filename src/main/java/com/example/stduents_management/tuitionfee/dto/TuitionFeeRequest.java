package com.example.stduents_management.tuitionfee.dto;

import com.example.stduents_management.tuitionfee.entity.TuitionFeeStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TuitionFeeRequest {

    @NotNull(message = "Chương trình đào tạo không được để trống")
    private UUID programId;

    @NotNull(message = "Học phí mỗi tín chỉ không được để trống")
    @DecimalMin(value = "1000", message = "Học phí phải ít nhất 1.000 VNĐ/tín chỉ")
    @DecimalMax(value = "99999999", message = "Học phí không vượt quá 99.999.999 VNĐ/tín chỉ")
    @Digits(integer = 8, fraction = 0, message = "Học phí phải là số nguyên (không có phần thập phân)")
    private BigDecimal feePerCredit;

    @NotNull(message = "Ngày bắt đầu áp dụng không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate effectiveDate;

    @NotNull(message = "Trạng thái không được để trống")
    private TuitionFeeStatus status;

    @Size(max = 500, message = "Ghi chú không quá 500 ký tự")
    private String note;

    /**
     * Khi bật cờ này, hệ thống sẽ tự động đặt mức học phí ACTIVE trước đó
     * của cùng chương trình về INACTIVE.
     */
    private boolean autoDeactivatePrevious = false;
}

package com.example.stduents_management.payment.dto;

import com.example.stduents_management.payment.entity.PaymentMethod;
import com.example.stduents_management.payment.entity.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentRequest {

    @NotNull(message = "Học phí học kỳ không được để trống")
    private UUID studentTuitionId;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "1", message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    private String transactionCode;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime paymentDate;

    @NotNull(message = "Trạng thái không được để trống")
    private PaymentStatus status = PaymentStatus.COMPLETED;

    public UUID getStudentTuitionId() {
        return studentTuitionId;
    }

    public void setStudentTuitionId(UUID studentTuitionId) {
        this.studentTuitionId = studentTuitionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}

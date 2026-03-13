package com.example.stduents_management.studenttuition.dto;

import com.example.stduents_management.studenttuition.entity.StudentTuitionStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class StudentTuitionRequest {

    @NotNull
    private UUID studentId;

    @NotNull
    private Long semesterId;

    @NotNull
    @Min(0)
    private Integer totalCredits;

    @NotNull
    @Min(0)
    private BigDecimal totalAmount;

    @NotNull
    @Min(0)
    private BigDecimal amountPaid;

    @NotNull
    private StudentTuitionStatus status = StudentTuitionStatus.UNPAID;

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public Long getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(Long semesterId) {
        this.semesterId = semesterId;
    }

    public Integer getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Integer totalCredits) {
        this.totalCredits = totalCredits;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public StudentTuitionStatus getStatus() {
        return status;
    }

    public void setStatus(StudentTuitionStatus status) {
        this.status = status;
    }
}


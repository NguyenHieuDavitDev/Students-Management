package com.example.stduents_management.graduationresult.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GraduationResultStatus {
    ELIGIBLE("Đủ điều kiện"),
    NOT_ELIGIBLE("Chưa đủ điều kiện"),
    PENDING("Chưa xét");

    private final String label;
}


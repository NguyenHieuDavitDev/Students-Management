package com.example.stduents_management.department.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class DepartmentResponse {
    private UUID departmentId;
    private String departmentCode;
    private String departmentName;
    private String description;
}

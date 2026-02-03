package com.example.stduents_management.role.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RoleResponse {
    private UUID id;
    private String name;
    private String description;
}

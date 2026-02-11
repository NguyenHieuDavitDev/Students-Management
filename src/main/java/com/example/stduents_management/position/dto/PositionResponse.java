package com.example.stduents_management.position.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PositionResponse {

    private UUID positionId;
    private String positionCode;
    private String positionName;
    private String description;
}

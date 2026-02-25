package com.example.stduents_management.building.dto;

import java.util.UUID;

public record BuildingResponse(
        UUID buildingId,
        String buildingCode,
        String buildingName,
        String address,
        Integer numberOfFloors,
        Double totalArea,
        String description
) {
}
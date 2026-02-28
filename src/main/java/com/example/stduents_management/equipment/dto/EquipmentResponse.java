package com.example.stduents_management.equipment.dto;

import com.example.stduents_management.equipment.entity.EquipmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EquipmentResponse(
        Long equipmentId,
        String equipmentCode,
        String equipmentName,
        String serialNumber,
        LocalDate purchaseDate,
        EquipmentStatus status,
        Long roomId,
        String roomCode,
        String roomName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

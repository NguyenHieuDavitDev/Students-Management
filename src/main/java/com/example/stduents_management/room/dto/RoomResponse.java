package com.example.stduents_management.room.dto;

import com.example.stduents_management.room.entity.RoomStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomResponse(
        Long roomId,
        String roomCode,
        String roomName,
        UUID buildingId,
        String buildingCode,
        String buildingName,
        UUID roomTypeId,
        String roomTypeCode,
        String roomTypeName,
        Integer floor,
        Integer capacity,
        Double area,
        RoomStatus status,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
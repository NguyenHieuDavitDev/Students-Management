package com.example.stduents_management.roomblocktime.dto;

import com.example.stduents_management.roomblocktime.entity.BlockStatus;
import com.example.stduents_management.roomblocktime.entity.BlockType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RoomBlockTimeResponse(
        UUID blockId,
        Long roomId,
        String roomCode,
        String roomName,
        BlockType blockType,
        Integer dayOfWeek,
        Integer timeSlotId,
        String timeSlotCode,
        Integer startWeek,
        Integer endWeek,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        BlockStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

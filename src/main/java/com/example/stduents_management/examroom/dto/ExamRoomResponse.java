package com.example.stduents_management.examroom.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExamRoomResponse(
    UUID id,
    Long roomId,
    String roomCode,
    String roomName,
    UUID buildingId,
    String buildingName,
    Integer examCapacity,
    Integer roomCapacity,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

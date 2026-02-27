package com.example.stduents_management.room.dto;

import com.example.stduents_management.room.entity.RoomStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class RoomRequest {

    @NotBlank
    private String roomCode;

    @NotBlank
    private String roomName;

    @NotNull
    private UUID buildingId;

    @NotNull
    private UUID roomTypeId;

    private Integer floor;

    @Min(1)
    private Integer capacity;

    @Positive
    private Double area;

    @NotNull
    private RoomStatus status;

    @NotNull
    private Boolean isActive;
}
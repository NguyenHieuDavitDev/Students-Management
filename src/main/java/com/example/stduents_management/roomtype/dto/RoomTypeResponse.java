package com.example.stduents_management.roomtype.dto;

import java.util.UUID;

public record RoomTypeResponse(
        UUID roomTypeId,
        String roomTypeCode,
        String roomTypeName,
        String description,
        Integer maxCapacity
) {}
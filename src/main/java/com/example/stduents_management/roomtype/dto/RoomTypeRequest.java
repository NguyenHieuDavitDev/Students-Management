package com.example.stduents_management.roomtype.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RoomTypeRequest {

    @NotBlank
    private String roomTypeCode;

    @NotBlank
    private String roomTypeName;

    private String description;

    @Min(1)
    private Integer maxCapacity;
}
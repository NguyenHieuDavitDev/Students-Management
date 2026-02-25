package com.example.stduents_management.building.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BuildingRequest {

    @NotBlank
    private String buildingCode;

    @NotBlank
    private String buildingName;

    private String address;

    @Min(1)
    private Integer numberOfFloors;

    private Double totalArea;

    private String description;
}

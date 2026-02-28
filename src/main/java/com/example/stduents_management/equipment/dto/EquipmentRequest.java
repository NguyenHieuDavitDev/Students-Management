package com.example.stduents_management.equipment.dto;

import com.example.stduents_management.equipment.entity.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EquipmentRequest {

    @NotBlank(message = "Mã thiết bị không được để trống")
    private String equipmentCode;

    @NotBlank(message = "Tên thiết bị không được để trống")
    private String equipmentName;

    private String serialNumber;

    private LocalDate purchaseDate;

    @NotNull(message = "Trạng thái không được để trống")
    private EquipmentStatus status;

    private Long roomId;
}

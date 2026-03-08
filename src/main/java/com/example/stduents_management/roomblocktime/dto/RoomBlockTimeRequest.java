package com.example.stduents_management.roomblocktime.dto;

import com.example.stduents_management.roomblocktime.entity.BlockStatus;
import com.example.stduents_management.roomblocktime.entity.BlockType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RoomBlockTimeRequest {

    @NotNull(message = "Phòng bị khóa không được để trống")
    private Long roomId;

    @NotNull(message = "Loại khóa không được để trống")
    private BlockType blockType;

    /** Thứ trong tuần (2–8). */
    @Min(2)
    @Max(8)
    private Integer dayOfWeek;

    private Integer timeSlotId;

    private Integer startWeek;
    private Integer endWeek;

    private LocalDate startDate;
    private LocalDate endDate;

    @Size(max = 255)
    private String reason;

    @NotNull(message = "Trạng thái không được để trống")
    private BlockStatus status = BlockStatus.ACTIVE;
}

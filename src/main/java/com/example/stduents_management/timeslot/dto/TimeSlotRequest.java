package com.example.stduents_management.timeslot.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeSlotRequest {

    @NotBlank(message = "Mã khung giờ không được để trống")
    @Size(max = 20)
    private String slotCode;

    @NotNull(message = "Tiết bắt đầu không được để trống")
    @Min(value = 1, message = "Tiết bắt đầu phải >= 1")
    private Integer periodStart;

    @NotNull(message = "Tiết kết thúc không được để trống")
    @Min(value = 1, message = "Tiết kết thúc phải >= 1")
    private Integer periodEnd;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private java.time.LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private java.time.LocalTime endTime;

    private Boolean isActive = true;
}

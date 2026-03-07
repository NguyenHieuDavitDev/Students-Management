package com.example.stduents_management.scheduleoverride.dto;

import com.example.stduents_management.scheduleoverride.entity.OverrideStatus;
import com.example.stduents_management.scheduleoverride.entity.OverrideType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ScheduleOverrideRequest {

    @NotNull(message = "Lịch gốc không được để trống")
    private UUID scheduleId;

    @NotNull(message = "Ngày áp dụng không được để trống")
    private LocalDate overrideDate;

    @NotNull(message = "Loại thay đổi không được để trống")
    private OverrideType overrideType;

    /** Phòng mới (bắt buộc nếu override_type = ROOM_CHANGE) */
    private Long newRoomId;

    /** Khung giờ mới (bắt buộc nếu override_type = TIME_CHANGE) */
    private Integer newTimeSlotId;

    /** Giảng viên thay thế (dạy bù) */
    private UUID newLecturerId;

    private OverrideStatus status = OverrideStatus.ACTIVE;

    @Size(max = 255)
    private String reason;
}

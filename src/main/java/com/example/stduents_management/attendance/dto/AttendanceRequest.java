package com.example.stduents_management.attendance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class AttendanceRequest {

    @NotNull(message = "Sinh viên không được để trống")
    private UUID studentId;

    @NotNull(message = "Lớp học phần không được để trống")
    private Long courseClassId;

    @NotNull(message = "Ngày điểm danh không được để trống")
    private LocalDate attendanceDate;

    @NotNull(message = "Trạng thái điểm danh không được để trống")
    private Boolean present;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;

    private UUID markedByLecturerId;
}


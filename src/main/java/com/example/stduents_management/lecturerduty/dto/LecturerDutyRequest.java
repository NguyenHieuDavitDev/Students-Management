package com.example.stduents_management.lecturerduty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LecturerDutyRequest {

    @NotBlank(message = "Mã chức vụ không được để trống")
    @Size(max = 20)
    private String dutyCode;

    @NotBlank(message = "Tên chức vụ không được để trống")
    @Size(max = 150)
    private String dutyName;

    @Size(max = 500)
    private String description;
}

package com.example.stduents_management.major.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class MajorRequest {

    @NotBlank(message = "Tên ngành không được để trống")
    @Size(max = 150, message = "Tên ngành tối đa 150 ký tự")
    private String majorName;

    @NotNull(message = "Vui lòng chọn khoa")
    private UUID facultyId;
}

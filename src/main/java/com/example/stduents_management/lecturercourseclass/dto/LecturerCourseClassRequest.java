package com.example.stduents_management.lecturercourseclass.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LecturerCourseClassRequest {

    @NotNull(message = "Lớp học phần không được để trống")
    private Long classSectionId;

    @NotNull(message = "Giảng viên không được để trống")
    private UUID lecturerId;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}


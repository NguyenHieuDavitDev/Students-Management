package com.example.stduents_management.examtype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamTypeRequest {

    @NotBlank(message = "Tên loại kỳ thi không được để trống")
    @Size(max = 200, message = "Tên loại kỳ thi tối đa 200 ký tự")
    private String name;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;
}

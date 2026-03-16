package com.example.stduents_management.examroom.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamRoomRequest {

    @NotNull(message = "Vui lòng chọn phòng")
    private Long roomId;

    private Integer examCapacity;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;
}

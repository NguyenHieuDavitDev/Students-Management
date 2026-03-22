package com.example.stduents_management.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FeedbackRequest {

    @NotNull(message = "Sinh viên không được để trống")
    private UUID studentId;

    @NotNull(message = "Giảng viên không được để trống")
    private UUID lecturerId;

    @NotNull(message = "Môn học không được để trống")
    private UUID subjectId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá từ 1 đến 5")
    @Max(value = 5, message = "Điểm đánh giá từ 1 đến 5")
    private Integer rating = 5;

    @Size(max = 1000, message = "Nhận xét tối đa 1000 ký tự")
    private String comment;
}

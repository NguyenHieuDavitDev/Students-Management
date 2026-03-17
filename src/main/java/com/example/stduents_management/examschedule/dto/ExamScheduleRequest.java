package com.example.stduents_management.examschedule.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class ExamScheduleRequest {

    @NotNull(message = "Vui lòng chọn lớp học phần")
    private Long classSectionId;   // class_sections.id (BIGINT)

    @NotNull(message = "Vui lòng chọn loại kỳ thi")
    private UUID examTypeId;       // exam_types.id (UUID)

    @NotNull(message = "Vui lòng chọn ngày thi")
    private LocalDate examDate;

    @NotNull(message = "Vui lòng chọn giờ thi")
    private LocalTime startTime;

    @NotNull(message = "Vui lòng nhập thời gian làm bài")
    @Min(value = 15, message = "Thời gian làm bài tối thiểu 15 phút")
    private Integer durationMinutes;

    private String note;
}
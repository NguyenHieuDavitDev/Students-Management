package com.example.stduents_management.classsection.dto;

import com.example.stduents_management.classsection.entity.ClassSectionStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class ClassSectionRequest {

    @AssertTrue(message = "Sĩ số hiện tại không được lớn hơn sĩ số tối đa")
    public boolean isValidStudentCount() {
        return maxStudents == null || currentStudents == null || currentStudents <= maxStudents;
    }

    @NotNull(message = "Môn học không được để trống")
    private UUID courseId;

    @NotNull(message = "Học kỳ không được để trống")
    private Long semesterId;

    @NotBlank(message = "Mã lớp không được để trống")
    @Size(max = 50)
    private String classCode;

    @NotBlank(message = "Tên lớp không được để trống")
    @Size(max = 200)
    private String className;

    @NotNull(message = "Sĩ số tối đa không được để trống")
    @Min(value = 1, message = "Sĩ số tối đa phải lớn hơn 0")
    private Integer maxStudents;

    @NotNull(message = "Sĩ số hiện tại không được để trống")
    @Min(value = 0, message = "Sĩ số hiện tại không được âm")
    private Integer currentStudents;

    @NotNull(message = "Trạng thái không được để trống")
    private ClassSectionStatus status;

    private Long roomId;

    @Size(max = 500)
    private String note;
}

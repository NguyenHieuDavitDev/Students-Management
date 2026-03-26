package com.example.stduents_management.lecturercourseclass.dto;

import java.util.UUID;

/** Giảng viên có ít nhất một lớp học phần gắn với lớp hành chính này. */
public record CohortTeachingLecturerRow(
        UUID lecturerId,
        String lecturerCode,
        String lecturerName
) {
}

package com.example.stduents_management.lecturercourseclass.dto;

import java.util.UUID;

/**
 * Một dòng phân công GV — lớp học phần + (tuỳ chọn) lớp hành chính, để hiển thị trên hồ sơ giảng viên.
 */
public record LecturerTeachingAssignmentRow(
        Long assignmentId,
        Long classSectionId,
        String sectionCode,
        String sectionName,
        String courseCode,
        String courseName,
        String semesterCode,
        UUID administrativeClassId,
        String administrativeClassCode,
        String administrativeClassName
) {
}

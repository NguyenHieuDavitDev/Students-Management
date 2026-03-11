package com.example.stduents_management.gradescale.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Kết quả bảng điểm tổng kết cho một sinh viên trong một lớp học phần.
 *
 * Quy trình:
 *   Bước 3 – Tính điểm tổng kết: totalScore10 = ∑(score_i × weight_i)
 *   Bước 4 – Tra bảng grade_scales → letterGrade, gradePoint (GPA), classification
 */
public record StudentTranscriptResult(
        UUID studentId,
        String studentCode,
        String studentName,
        Long courseClassId,
        String classCode,
        String className,
        String courseName,

        List<GradeLineDetail> gradeLines,   // Chi tiết điểm từng thành phần (Step 2 data)

        BigDecimal totalScore10,            // Điểm tổng kết hệ 10 (null nếu chưa nhập đủ)
        BigDecimal gradePoint,              // Điểm GPA hệ 4 (từ grade_scales, Step 4)
        String letterGrade,                 // Điểm chữ (từ grade_scales, Step 4)
        String classification,              // Xếp loại – description (từ grade_scales, Step 4)
        boolean scaleFound,                 // true nếu tìm được thang điểm tương ứng
        boolean fullyGraded                 // true nếu đã nhập đủ điểm tất cả thành phần
) {
}

package com.example.stduents_management.gradescale.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Chi tiết một thành phần điểm của sinh viên trong bảng điểm tổng kết.
 * Đại diện cho một dòng: tên thành phần, trọng số, điểm đạt được, đóng góp vào tổng.
 */
public record GradeLineDetail(
        UUID gradeComponentId,
        String componentName,
        BigDecimal weight,        // Trọng số, ví dụ 0.50 = 50%
        BigDecimal maxScore,      // Điểm tối đa của thành phần
        BigDecimal score,         // Điểm thực tế (null nếu chưa nhập)
        BigDecimal weightedScore  // = score × weight (null nếu chưa có điểm)
) {
    /** Trọng số dưới dạng phần trăm (đã lưu dạng %, ví dụ: 50 → "50%"). */
    public BigDecimal weightPercent() {
        return weight; // weight đã là % (0–100), dùng trực tiếp
    }
}

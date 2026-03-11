package com.example.stduents_management.gradescale.service;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.courseregistration.entity.CourseRegistration;
import com.example.stduents_management.courseregistration.repository.CourseRegistrationRepository;
import com.example.stduents_management.gradecomponent.entity.GradeComponent;
import com.example.stduents_management.gradecomponent.repository.GradeComponentRepository;
import com.example.stduents_management.gradescale.dto.GradeLineDetail;
import com.example.stduents_management.gradescale.dto.StudentTranscriptResult;
import com.example.stduents_management.gradescale.entity.GradeScale;
import com.example.stduents_management.gradescale.repository.GradeScaleRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.studentgrade.entity.StudentGrade;
import com.example.stduents_management.studentgrade.repository.StudentGradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Dịch vụ tính điểm tổng kết và quy đổi thang điểm.
 *
 * Quy trình:
 *   Bước 1 – Admin đã tạo thành phần điểm (grade_components) có weight.
 *   Bước 2 – Giảng viên đã nhập điểm (student_grades) cho từng thành phần.
 *   Bước 3 – Service này tính: totalScore = ∑(score_i × weight_i)
 *   Bước 4 – Tra bảng grade_scales → letterGrade, GPA, xếp loại.
 */
@Service
@RequiredArgsConstructor
public class GradeTranscriptService {

    private final StudentGradeRepository studentGradeRepository;
    private final GradeComponentRepository gradeComponentRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final GradeScaleRepository gradeScaleRepository;
    private final ClassSectionRepository classSectionRepository;

    // ─── Tính điểm tổng kết cho toàn bộ lớp học phần ────────────────────────

    /**
     * Tính bảng điểm tổng kết cho tất cả sinh viên trong một lớp học phần.
     * Sử dụng batch-load để tránh N+1 query.
     *
     * @param courseClassId ID của lớp học phần (ClassSection.id)
     * @return Danh sách kết quả theo thứ tự tên sinh viên
     */
    public List<StudentTranscriptResult> calculateForClass(Long courseClassId) {
        // Validate lớp học phần tồn tại
        ClassSection classSection = classSectionRepository.findById(courseClassId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));

        // Bước 1 data: lấy tất cả thành phần điểm (có trọng số)
        List<GradeComponent> components =
                gradeComponentRepository.findByClassSection_IdOrderByComponentName(courseClassId);

        // Bước 2 data: lấy tất cả điểm đã nhập của lớp này (batch load, tránh N+1)
        List<StudentGrade> allGrades = studentGradeRepository.findAllByCourseClass_Id(courseClassId);

        // Nhóm điểm theo: studentId → (componentId → StudentGrade)
        Map<UUID, Map<UUID, StudentGrade>> gradesByStudent = new HashMap<>();
        for (StudentGrade sg : allGrades) {
            UUID sid = sg.getStudent().getStudentId();
            UUID cid = sg.getGradeComponent().getId();
            gradesByStudent.computeIfAbsent(sid, k -> new HashMap<>()).put(cid, sg);
        }

        // Lấy danh sách sinh viên đã đăng ký lớp này, sắp xếp theo tên
        List<CourseRegistration> registrations =
                courseRegistrationRepository.findByClassSection_IdOrderByStudent_FullName(courseClassId);

        List<StudentTranscriptResult> results = new ArrayList<>();
        for (CourseRegistration reg : registrations) {
            Student student = reg.getStudent();
            Map<UUID, StudentGrade> studentGrades = gradesByStudent
                    .getOrDefault(student.getStudentId(), Collections.emptyMap());
            results.add(buildTranscript(student, classSection, components, studentGrades));
        }
        return results;
    }

    /**
     * Tính bảng điểm tổng kết cho một sinh viên trong một lớp học phần.
     *
     * @param studentId     UUID của sinh viên
     * @param courseClassId ID của lớp học phần
     */
    public StudentTranscriptResult calculateForStudent(UUID studentId, Long courseClassId) {
        ClassSection classSection = classSectionRepository.findById(courseClassId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));

        List<GradeComponent> components =
                gradeComponentRepository.findByClassSection_IdOrderByComponentName(courseClassId);

        List<StudentGrade> grades =
                studentGradeRepository.findAllByStudent_StudentIdAndCourseClass_Id(studentId, courseClassId);

        Map<UUID, StudentGrade> gradeMap = new HashMap<>();
        for (StudentGrade sg : grades) {
            gradeMap.put(sg.getGradeComponent().getId(), sg);
        }

        // Lấy thông tin sinh viên từ điểm hoặc từ đăng ký
        Student student = null;
        if (!grades.isEmpty()) {
            student = grades.get(0).getStudent();
        } else {
            List<CourseRegistration> regs =
                    courseRegistrationRepository.findByClassSection_IdOrderByStudent_FullName(courseClassId);
            for (CourseRegistration r : regs) {
                if (r.getStudent().getStudentId().equals(studentId)) {
                    student = r.getStudent();
                    break;
                }
            }
        }

        return buildTranscript(student, classSection, components, gradeMap);
    }

    /**
     * Lấy danh sách tất cả lớp học phần (cho dropdown selector).
     */
    public List<ClassSection> getAllClasses() {
        return classSectionRepository.findAll(Sort.by(Sort.Direction.ASC, "classCode"));
    }

    /**
     * Lấy danh sách các thành phần điểm của lớp học phần (cho header bảng).
     */
    public List<GradeComponent> getComponents(Long courseClassId) {
        return gradeComponentRepository.findByClassSection_IdOrderByComponentName(courseClassId);
    }

    // ─── Logic tính điểm (Bước 3 + Bước 4) ──────────────────────────────────

    private StudentTranscriptResult buildTranscript(
            Student student,
            ClassSection classSection,
            List<GradeComponent> components,
            Map<UUID, StudentGrade> gradeMap) {

        List<GradeLineDetail> lines = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        boolean fullyGraded = !components.isEmpty(); // false nếu không có thành phần nào

        for (GradeComponent comp : components) {
            StudentGrade grade = gradeMap.get(comp.getId());
            BigDecimal score = (grade != null) ? grade.getScore() : null;
            BigDecimal weight = comp.getWeight();
            BigDecimal weightedScore = null;

            if (score == null) {
                fullyGraded = false;
            } else if (weight != null) {
                // Bước 3: weight lưu dạng % (0–100), chia 100 trước khi nhân
                // Ví dụ: weight=50 → 50/100=0.50 → score × 0.50
                BigDecimal fraction = weight.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
                weightedScore = score.multiply(fraction).setScale(4, RoundingMode.HALF_UP);
                totalScore = totalScore.add(weightedScore);
            }

            lines.add(new GradeLineDetail(
                    comp.getId(),
                    comp.getComponentName(),
                    weight,
                    comp.getMaxScore(),
                    score,
                    weightedScore
            ));
        }

        // Điểm tổng kết hệ 10 (làm tròn 2 chữ số thập phân)
        BigDecimal total10 = fullyGraded
                ? totalScore.setScale(2, RoundingMode.HALF_UP)
                : null;

        // Bước 4: tra bảng grade_scales
        Optional<GradeScale> scale = (total10 != null)
                ? gradeScaleRepository.findByScore(total10)
                : Optional.empty();

        return new StudentTranscriptResult(
                student != null ? student.getStudentId() : null,
                student != null ? student.getStudentCode() : null,
                student != null ? student.getFullName() : null,
                classSection.getId(),
                classSection.getClassCode(),
                classSection.getClassName(),
                classSection.getCourse() != null ? classSection.getCourse().getCourseName() : null,
                lines,
                total10,
                scale.map(GradeScale::getGradePoint).orElse(null),
                scale.map(GradeScale::getLetterGrade).orElse(null),
                scale.map(GradeScale::getDescription).orElse(null),
                scale.isPresent(),
                fullyGraded
        );
    }
}

package com.example.stduents_management.web;

import com.example.stduents_management.classroom.repository.ClassRepository;
import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.faculty.repository.FacultyRepository;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.student.repository.StudentRepository;
import com.example.stduents_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thống kê cho trang Overview: dữ liệu biểu đồ cột, đường, tròn.
 */
@Service
@RequiredArgsConstructor
public class OverviewStatsService {

    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final ClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // --- Biểu đồ cột: số lượng theo loại (Sinh viên, Giảng viên, Lớp, Khóa học, Khoa, User)
        List<String> barLabels = List.of("Sinh viên", "Giảng viên", "Lớp", "Khóa học", "Khoa", "User");
        List<Long> barData = List.of(
                studentRepository.count(),
                lecturerRepository.count(),
                classRepository.count(),
                courseRepository.count(),
                facultyRepository.count(),
                userRepository.count()
        );
        stats.put("barChartLabels", barLabels);
        stats.put("barChartData", barData);

        // --- Biểu đồ đường: số lớp theo năm học
        List<Object[]> classesByYear = classRepository.countClassesByAcademicYear();
        List<String> lineLabels = new ArrayList<>();
        List<Long> lineData = new ArrayList<>();
        for (Object[] row : classesByYear) {
            lineLabels.add((String) row[0]);
            lineData.add(((Number) row[1]).longValue());
        }
        stats.put("lineChartLabels", lineLabels);
        stats.put("lineChartData", lineData);

        // --- Biểu đồ tròn: số sinh viên theo khoa
        List<Object[]> studentsByFaculty = studentRepository.countStudentsByFaculty();
        List<String> pieLabels = new ArrayList<>();
        List<Long> pieData = new ArrayList<>();
        for (Object[] row : studentsByFaculty) {
            pieLabels.add((String) row[0]);
            pieData.add(((Number) row[1]).longValue());
        }
        stats.put("pieChartLabels", pieLabels);
        stats.put("pieChartData", pieData);

        return stats;
    }
}

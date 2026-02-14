package com.example.stduents_management.course.service;

import com.example.stduents_management.course.dto.CourseRequest;
import com.example.stduents_management.course.dto.CourseResponse;
import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.faculty.entity.Faculty;
import com.example.stduents_management.faculty.repository.FacultyRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;

    /* ================= SEARCH + PAGING ================= */
    public Page<CourseResponse> search(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("courseName"));

        Page<Course> courses;

        if (keyword == null || keyword.isBlank()) {
            courses = courseRepository.findAll(pageable);
        } else {
            courses =
                    courseRepository.findByCourseCodeContainingIgnoreCaseOrCourseNameContainingIgnoreCaseOrFaculty_FacultyNameContainingIgnoreCase(
                            keyword, keyword, keyword, pageable);
        }

        return courses.map(this::mapToResponse);
    }

    /* ================= GET BY ID ================= */
    public CourseResponse getById(UUID id) {
        Course c = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần"));
        return mapToResponse(c);
    }

    /* ================= CREATE ================= */
    @Transactional
    public CourseResponse create(CourseRequest req) {

        if (courseRepository.existsByCourseCodeIgnoreCase(req.getCourseCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã học phần đã tồn tại");
        }

        Faculty faculty = facultyRepository.findById(req.getFacultyId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khoa"));

        Course c = new Course();
        c.setCourseCode(req.getCourseCode().trim());
        c.setCourseName(req.getCourseName().trim());
        c.setCredits(req.getCredits());
        c.setLectureHours(req.getLectureHours());
        c.setPracticeHours(req.getPracticeHours());
        c.setFaculty(faculty);
        c.setDescription(req.getDescription());
        c.setStatus(req.getStatus());

        return mapToResponse(courseRepository.save(c));
    }

    /* ================= UPDATE ================= */
    @Transactional
    public CourseResponse update(UUID id, CourseRequest req) {

        Course c = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần"));

        if (courseRepository.existsByCourseCodeIgnoreCaseAndIdNot(req.getCourseCode(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã học phần đã tồn tại");
        }

        Faculty faculty = facultyRepository.findById(req.getFacultyId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khoa"));

        c.setCourseCode(req.getCourseCode().trim());
        c.setCourseName(req.getCourseName().trim());
        c.setCredits(req.getCredits());
        c.setLectureHours(req.getLectureHours());
        c.setPracticeHours(req.getPracticeHours());
        c.setFaculty(faculty);
        c.setDescription(req.getDescription());
        c.setStatus(req.getStatus());

        return mapToResponse(courseRepository.save(c));
    }

    /* ================= DELETE ================= */
    @Transactional
    public void delete(UUID id) {

        if (!courseRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy học phần");
        }

        courseRepository.deleteById(id);
    }

    /* ================= EXPORT EXCEL ================= */
    public void exportExcel(HttpServletResponse response) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Courses");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã học phần");
            header.createCell(1).setCellValue("Tên học phần");
            header.createCell(2).setCellValue("Số tín chỉ");
            header.createCell(3).setCellValue("Giờ lý thuyết");
            header.createCell(4).setCellValue("Giờ thực hành");
            header.createCell(5).setCellValue("Khoa");
            header.createCell(6).setCellValue("Mô tả");
            header.createCell(7).setCellValue("Trạng thái");

            List<Course> courses = courseRepository.findAll(Sort.by("courseName"));

            int rowIndex = 1;

            for (Course c : courses) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(c.getCourseCode());
                row.createCell(1).setCellValue(c.getCourseName());
                row.createCell(2).setCellValue(c.getCredits() != null ? c.getCredits() : 0);
                row.createCell(3).setCellValue(c.getLectureHours() != null ? c.getLectureHours() : 0);
                row.createCell(4).setCellValue(c.getPracticeHours() != null ? c.getPracticeHours() : 0);
                row.createCell(5).setCellValue(
                        c.getFaculty() != null ? c.getFaculty().getFacultyName() : "");
                row.createCell(6).setCellValue(
                        c.getDescription() != null ? c.getDescription() : "");
                row.createCell(7).setCellValue(
                        Boolean.TRUE.equals(c.getStatus()) ? "Hoạt động" : "Ngừng");
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=courses.xlsx");

            workbook.write(response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể export Excel");
        }
    }

    /* ================= IMPORT EXCEL ================= */
    @Transactional
    public int importExcel(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File rỗng");
        }

        int count = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);

            // Cache faculty theo tên để tránh query nhiều lần
            Map<String, Faculty> facultyMap = new HashMap<>();
            facultyRepository.findAll().forEach(f ->
                    facultyMap.put(f.getFacultyName().toLowerCase(), f));

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                String code = row.getCell(0).getStringCellValue().trim();
                String name = row.getCell(1).getStringCellValue().trim();
                String facultyName = row.getCell(5).getStringCellValue().trim().toLowerCase();

                if (courseRepository.existsByCourseCodeIgnoreCase(code)) continue;

                Faculty faculty = facultyMap.get(facultyName);
                if (faculty == null) continue;

                Course c = new Course();
                c.setCourseCode(code);
                c.setCourseName(name);
                c.setFaculty(faculty);
                c.setStatus(true);

                courseRepository.save(c);
                count++;
            }

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ");
        }

        return count;
    }

    /* ================= PRINT ================= */
    public List<CourseResponse> getForPrint() {

        return courseRepository.findAll(Sort.by("courseName"))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /* ================= MAPPER ================= */
    private CourseResponse mapToResponse(Course c) {

        Faculty faculty = c.getFaculty();

        return new CourseResponse(
                c.getId(),
                c.getCourseCode(),
                c.getCourseName(),
                c.getCredits(),
                c.getLectureHours(),
                c.getPracticeHours(),
                faculty != null ? faculty.getFacultyId() : null,
                faculty != null ? faculty.getFacultyName() : null,
                c.getDescription(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
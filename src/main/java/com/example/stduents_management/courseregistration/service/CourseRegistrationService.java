package com.example.stduents_management.courseregistration.service;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.entity.ClassSectionStatus;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.courseregistration.dto.CourseRegistrationRequest;
import com.example.stduents_management.courseregistration.dto.CourseRegistrationResponse;
import com.example.stduents_management.courseregistration.entity.CourseRegistration;
import com.example.stduents_management.courseregistration.repository.CourseRegistrationRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseRegistrationService {

    private final CourseRegistrationRepository repository;
    private final StudentRepository studentRepository;
    private final ClassSectionRepository classSectionRepository;

    public Page<CourseRegistrationResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "registeredAt"));
        Page<CourseRegistration> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository.search(keyword, pageable);
        return data.map(this::toResponse);
    }

    public CourseRegistrationResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký học phần"));
    }

    @Transactional
    public void create(CourseRegistrationRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên"));

        ClassSection section = classSectionRepository.findById(req.getClassSectionId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));

        validateBusinessRules(student.getStudentId(), section);

        CourseRegistration reg = new CourseRegistration();
        reg.setStudent(student);
        reg.setClassSection(section);
        reg.setRegisteredAt(LocalDateTime.now());
        reg.setNote(req.getNote());

        repository.save(reg);
        increaseCurrentStudents(section);
    }

    @Transactional
    public void update(Long id, CourseRegistrationRequest req) {
        CourseRegistration reg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký học phần"));

        UUID newStudentId = req.getStudentId();
        Long newSectionId = req.getClassSectionId();

        if (newStudentId == null || newSectionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu thông tin sinh viên hoặc lớp học phần");
        }

        Student student = studentRepository.findById(newStudentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên"));
        ClassSection section = classSectionRepository.findById(newSectionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));

        boolean sectionChanged = !reg.getClassSection().getId().equals(newSectionId);

        if (sectionChanged) {
            validateBusinessRules(student.getStudentId(), section);
        } else if (!reg.getStudent().getStudentId().equals(newStudentId)
                && repository.existsByStudent_StudentIdAndClassSection_Id(newStudentId, newSectionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sinh viên đã được đăng ký vào lớp học phần này");
        }

        if (sectionChanged) {
            decreaseCurrentStudents(reg.getClassSection());
            increaseCurrentStudents(section);
        }

        reg.setStudent(student);
        reg.setClassSection(section);
        reg.setNote(req.getNote());
    }

    @Transactional
    public void delete(Long id) {
        CourseRegistration reg = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy đăng ký học phần"));
        ClassSection section = reg.getClassSection();
        repository.delete(reg);
        decreaseCurrentStudents(section);
    }

    public List<CourseRegistrationResponse> getForPrint() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "registeredAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        DataFormatter formatter = new DataFormatter(Locale.getDefault());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                final int rowNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String studentCode = readString(row, 0, formatter);
                String classCode = readString(row, 1, formatter);
                String note = readString(row, 2, formatter);

                if (studentCode == null || studentCode.isBlank()
                        || classCode == null || classCode.isBlank()) {
                    continue;
                }

                Student student = studentRepository.findByStudentCodeIgnoreCase(studentCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy sinh viên " + studentCode));

                ClassSection section = classSectionRepository.findByClassCodeIgnoreCase(classCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy lớp học phần " + classCode));

                validateBusinessRules(student.getStudentId(), section);

                if (repository.existsByStudent_StudentIdAndClassSection_Id(student.getStudentId(), section.getId())) {
                    continue;
                }

                CourseRegistration reg = new CourseRegistration();
                reg.setStudent(student);
                reg.setClassSection(section);
                reg.setRegisteredAt(LocalDateTime.now());
                reg.setNote(note);
                repository.save(reg);
                increaseCurrentStudents(section);
            }
        }
    }

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("CourseRegistrations");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Student Code");
            header.createCell(1).setCellValue("Student Name");
            header.createCell(2).setCellValue("Class Code");
            header.createCell(3).setCellValue("Class Name");
            header.createCell(4).setCellValue("Course Code");
            header.createCell(5).setCellValue("Course Name");
            header.createCell(6).setCellValue("Semester Code");
            header.createCell(7).setCellValue("Registered At");
            header.createCell(8).setCellValue("Note");

            List<CourseRegistration> list = repository.findAll(Sort.by(Sort.Direction.DESC, "registeredAt"));

            int rowNum = 1;
            for (CourseRegistration reg : list) {
                Row row = sheet.createRow(rowNum++);
                CourseRegistrationResponse r = toResponse(reg);
                row.createCell(0).setCellValue(nullToEmpty(r.studentCode()));
                row.createCell(1).setCellValue(nullToEmpty(r.studentName()));
                row.createCell(2).setCellValue(nullToEmpty(r.classCode()));
                row.createCell(3).setCellValue(nullToEmpty(r.className()));
                row.createCell(4).setCellValue(nullToEmpty(r.courseCode()));
                row.createCell(5).setCellValue(nullToEmpty(r.courseName()));
                row.createCell(6).setCellValue(nullToEmpty(r.semesterCode()));
                row.createCell(7).setCellValue(r.registeredAt() != null ? r.registeredAt().toString() : "");
                row.createCell(8).setCellValue(nullToEmpty(r.note()));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void validateBusinessRules(UUID studentId, ClassSection section) {
        if (section.getStatus() == ClassSectionStatus.CLOSED || section.getStatus() == ClassSectionStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lớp học phần không còn mở đăng ký");
        }
        long current = repository.countByClassSection_Id(section.getId());
        int max = section.getMaxStudents() != null ? section.getMaxStudents() : 0;
        if (max > 0 && current >= max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lớp học phần đã đủ sĩ số");
        }
        if (repository.existsByStudent_StudentIdAndClassSection_Id(studentId, section.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sinh viên đã được đăng ký vào lớp học phần này");
        }
    }

    private void increaseCurrentStudents(ClassSection section) {
        Integer current = section.getCurrentStudents();
        section.setCurrentStudents((current != null ? current : 0) + 1);
    }

    private void decreaseCurrentStudents(ClassSection section) {
        Integer current = section.getCurrentStudents();
        if (current == null || current <= 0) {
            section.setCurrentStudents(0);
        } else {
            section.setCurrentStudents(current - 1);
        }
    }

    private CourseRegistrationResponse toResponse(CourseRegistration reg) {
        Student s = reg.getStudent();
        ClassSection cs = reg.getClassSection();
        return new CourseRegistrationResponse(
                reg.getId(),
                s != null ? s.getStudentId() : null,
                s != null ? s.getStudentCode() : null,
                s != null ? s.getFullName() : null,
                cs != null ? cs.getId() : null,
                cs != null ? cs.getClassCode() : null,
                cs != null ? cs.getClassName() : null,
                cs != null && cs.getCourse() != null ? cs.getCourse().getCourseCode() : null,
                cs != null && cs.getCourse() != null ? cs.getCourse().getCourseName() : null,
                cs != null && cs.getSemester() != null ? cs.getSemester().getCode() : null,
                cs != null && cs.getSemester() != null ? cs.getSemester().getName() : null,
                reg.getRegisteredAt(),
                reg.getNote()
        );
    }

    private String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}


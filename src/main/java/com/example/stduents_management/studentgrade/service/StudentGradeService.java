package com.example.stduents_management.studentgrade.service;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.courseregistration.repository.CourseRegistrationRepository;
import com.example.stduents_management.gradecomponent.entity.GradeComponent;
import com.example.stduents_management.gradecomponent.repository.GradeComponentRepository;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.student.repository.StudentRepository;
import com.example.stduents_management.studentgrade.dto.StudentGradeRequest;
import com.example.stduents_management.studentgrade.dto.StudentGradeResponse;
import com.example.stduents_management.studentgrade.entity.StudentGrade;
import com.example.stduents_management.studentgrade.repository.StudentGradeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentGradeService {

    private final StudentGradeRepository studentGradeRepository;
    private final StudentRepository studentRepository;
    private final ClassSectionRepository classSectionRepository;
    private final GradeComponentRepository gradeComponentRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final LecturerRepository lecturerRepository;

    public Page<StudentGradeResponse> search(String keyword, Long courseClassId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        String searchTerm = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<StudentGrade> data = courseClassId != null
                ? studentGradeRepository.searchByCourseClassAndKeyword(courseClassId, searchTerm, pageable)
                : studentGradeRepository.searchByKeyword(searchTerm, pageable);
        return data.map(this::toResponse);
    }

    public StudentGradeResponse getById(UUID id) {
        return studentGradeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi điểm"));
    }

    @Transactional
    public void save(StudentGradeRequest req, Optional<UUID> gradedByLecturerId) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên"));
        ClassSection courseClass = classSectionRepository.findById(req.getCourseClassId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
        GradeComponent gradeComponent = gradeComponentRepository.findById(req.getGradeComponentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thành phần điểm"));

        if (!gradeComponent.getClassSection().getId().equals(courseClass.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Thành phần điểm không thuộc lớp học phần đã chọn");
        }
        if (!courseRegistrationRepository.existsByStudent_StudentIdAndClassSection_Id(
                req.getStudentId(), req.getCourseClassId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sinh viên chưa đăng ký lớp học phần này");
        }
        if (req.getScore() != null && gradeComponent.getMaxScore() != null
                && req.getScore().compareTo(gradeComponent.getMaxScore()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Điểm không được vượt quá điểm tối đa (" + gradeComponent.getMaxScore() + ")");
        }

        StudentGrade entity = studentGradeRepository
                .findByStudent_StudentIdAndCourseClass_IdAndGradeComponent_Id(
                        req.getStudentId(), req.getCourseClassId(), req.getGradeComponentId())
                .orElse(new StudentGrade());

        if (entity.getId() == null) {
            entity.setStudent(student);
            entity.setCourseClass(courseClass);
            entity.setGradeComponent(gradeComponent);
        }
        entity.setScore(req.getScore());
        if (gradedByLecturerId.isPresent()) {
            Lecturer lecturer = lecturerRepository.findById(gradedByLecturerId.get())
                    .orElse(null);
            entity.setGradedBy(lecturer);
        }
        entity.setGradedAt(java.time.LocalDateTime.now());
        studentGradeRepository.save(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!studentGradeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi điểm");
        }
        studentGradeRepository.deleteById(id);
    }

    public List<StudentGradeResponse> getForPrint() {
        return studentGradeRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StudentGradeResponse> getForPrintByCourseClass(Long courseClassId) {
        return studentGradeRepository.searchByCourseClassAndKeyword(courseClassId, null,
                        Pageable.unpaged(Sort.by("student.fullName", "gradeComponent.componentName")))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void importExcel(MultipartFile file, Optional<UUID> defaultGradedByLecturerId) throws Exception {
        DataFormatter formatter = new DataFormatter(Locale.getDefault());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                int rowNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String studentCode = readString(row, 0, formatter);
                if (studentCode == null || studentCode.isBlank()) continue;

                String classCode = readString(row, 1, formatter);
                if (classCode == null || classCode.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Mã lớp học phần không được để trống");
                }

                String componentName = readString(row, 2, formatter);
                if (componentName == null || componentName.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Tên thành phần điểm không được để trống");
                }

                BigDecimal score = readBigDecimal(row, 3, formatter);
                String lecturerCode = readString(row, 4, formatter);

                Student student = studentRepository.findByStudentCodeIgnoreCase(studentCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy sinh viên với mã " + studentCode));

                ClassSection courseClass = classSectionRepository.findByClassCodeIgnoreCase(classCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy lớp học phần với mã " + classCode));

                GradeComponent gradeComponent = gradeComponentRepository
                        .findByClassSection_IdAndComponentNameIgnoreCase(courseClass.getId(), componentName.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy thành phần điểm '" + componentName + "' trong lớp " + classCode));

                if (!courseRegistrationRepository.existsByStudent_StudentIdAndClassSection_Id(
                        student.getStudentId(), courseClass.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Sinh viên " + studentCode + " chưa đăng ký lớp " + classCode);
                }

                Optional<UUID> gradedBy = Optional.empty();
                if (lecturerCode != null && !lecturerCode.isBlank()) {
                    gradedBy = lecturerRepository.findByLecturerCodeIgnoreCase(lecturerCode.trim())
                            .map(l -> Optional.of(l.getLecturerId()))
                            .orElse(Optional.empty());
                }
                if (gradedBy.isEmpty()) {
                    gradedBy = defaultGradedByLecturerId;
                }

                StudentGrade entity = studentGradeRepository
                        .findByStudent_StudentIdAndCourseClass_IdAndGradeComponent_Id(
                                student.getStudentId(), courseClass.getId(), gradeComponent.getId())
                        .orElse(new StudentGrade());

                if (entity.getId() == null) {
                    entity.setStudent(student);
                    entity.setCourseClass(courseClass);
                    entity.setGradeComponent(gradeComponent);
                }
                entity.setScore(score);
                if (gradedBy.isPresent()) {
                    entity.setGradedBy(lecturerRepository.findById(gradedBy.get()).orElse(null));
                }
                entity.setGradedAt(java.time.LocalDateTime.now());
                studentGradeRepository.save(entity);
            }
        }
    }

    public byte[] exportExcel(Long courseClassId) throws Exception {
        List<StudentGradeResponse> list = courseClassId != null
                ? getForPrintByCourseClass(courseClassId)
                : getForPrint();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Điểm sinh viên");
            Row header = sheet.createRow(0);
            String[] headers = {"Mã SV", "Họ tên", "Mã lớp HP", "Lớp HP", "Học phần", "Thành phần điểm", "Điểm", "GV nhập điểm", "Thời gian"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (StudentGradeResponse g : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(g.studentCode() != null ? g.studentCode() : "");
                row.createCell(1).setCellValue(g.studentName() != null ? g.studentName() : "");
                row.createCell(2).setCellValue(g.classCode() != null ? g.classCode() : "");
                row.createCell(3).setCellValue(g.className() != null ? g.className() : "");
                row.createCell(4).setCellValue(g.courseName() != null ? g.courseName() : "");
                row.createCell(5).setCellValue(g.componentName() != null ? g.componentName() : "");
                row.createCell(6).setCellValue(g.score() != null ? g.score().doubleValue() : 0);
                row.createCell(7).setCellValue(g.gradedByLecturerName() != null ? g.gradedByLecturerName() : "");
                row.createCell(8).setCellValue(g.gradedAt() != null ? g.gradedAt().toString() : "");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] getImportTemplate() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Mẫu nhập điểm");
            Row header = sheet.createRow(0);
            String[] headers = {"Mã sinh viên", "Mã lớp học phần", "Tên thành phần điểm", "Điểm", "Mã GV (tùy chọn)"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("SV001");
            example.createCell(1).setCellValue("CLC01");
            example.createCell(2).setCellValue("Điểm giữa kỳ");
            example.createCell(3).setCellValue(8.5);
            example.createCell(4).setCellValue("GV001");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }

    private BigDecimal readBigDecimal(Row row, int cellIndex, DataFormatter formatter) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return null;
        try {
            return new BigDecimal(raw.replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private StudentGradeResponse toResponse(StudentGrade sg) {
        Student s = sg.getStudent();
        ClassSection cc = sg.getCourseClass();
        GradeComponent gc = sg.getGradeComponent();
        Lecturer gb = sg.getGradedBy();
        return new StudentGradeResponse(
                sg.getId(),
                s != null ? s.getStudentId() : null,
                s != null ? s.getStudentCode() : null,
                s != null ? s.getFullName() : null,
                cc != null ? cc.getId() : null,
                cc != null ? cc.getClassCode() : null,
                cc != null ? cc.getClassName() : null,
                cc != null && cc.getCourse() != null ? cc.getCourse().getCourseCode() : null,
                cc != null && cc.getCourse() != null ? cc.getCourse().getCourseName() : null,
                gc != null ? gc.getId() : null,
                gc != null ? gc.getComponentName() : null,
                gc != null ? gc.getMaxScore() : null,
                sg.getScore(),
                gb != null ? gb.getLecturerId() : null,
                gb != null ? gb.getFullName() : null,
                sg.getGradedAt(),
                sg.getUpdatedAt()
        );
    }
}

package com.example.stduents_management.lecturercourseclass.service;

import com.example.stduents_management.classroom.entity.ClassEntity;
import com.example.stduents_management.classroom.repository.ClassRepository;
import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.lecturercourseclass.dto.CohortTeachingLecturerRow;
import com.example.stduents_management.lecturercourseclass.dto.LecturerCourseClassRequest;
import com.example.stduents_management.lecturercourseclass.dto.LecturerCourseClassResponse;
import com.example.stduents_management.lecturercourseclass.dto.LecturerTeachingAssignmentRow;
import com.example.stduents_management.lecturercourseclass.entity.LecturerCourseClass;
import com.example.stduents_management.lecturercourseclass.repository.LecturerCourseClassRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LecturerCourseClassService {

    private final LecturerCourseClassRepository repository;
    private final ClassSectionRepository classSectionRepository;
    private final ClassRepository classRepository;
    private final LecturerRepository lecturerRepository;

    @Transactional(readOnly = true)
    public Page<LecturerCourseClassResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<LecturerCourseClass> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository.search(keyword, pageable);
        return data.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public LecturerCourseClassResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công giảng viên"));
    }

    @Transactional
    public void create(LecturerCourseClassRequest req) {
        validateDuplicate(req.getClassSectionId(), req.getLecturerId(), null);
        repository.save(build(new LecturerCourseClass(), req));
    }

    @Transactional
    public void update(Long id, LecturerCourseClassRequest req) {
        LecturerCourseClass entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công giảng viên"));
        validateDuplicate(req.getClassSectionId(), req.getLecturerId(), id);
        build(entity, req);
        repository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phân công giảng viên");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<LecturerCourseClassResponse> getForPrint() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "classSection.classCode")
                        .and(Sort.by("lecturer.lecturerCode")))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File import rỗng");
        }
        DataFormatter formatter = new DataFormatter(Locale.getDefault());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                final int rowNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String classCode = readString(row, 0, formatter);
                String lecturerCode = readString(row, 1, formatter);
                String note = readString(row, 2, formatter);

                if (classCode == null || classCode.isBlank()) continue;
                if (lecturerCode == null || lecturerCode.isBlank()) continue;

                ClassSection cs = classSectionRepository.findAll().stream()
                        .filter(c -> c.getClassCode().equalsIgnoreCase(classCode.trim()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy lớp học phần " + classCode));

                ensureAdministrativeClassLinked(cs);

                Lecturer lecturer = lecturerRepository.findByLecturerCodeIgnoreCase(lecturerCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy giảng viên " + lecturerCode));

                if (repository.existsByClassSection_IdAndLecturer_LecturerId(cs.getId(), lecturer.getLecturerId())) {
                    continue;
                }

                LecturerCourseClass entity = new LecturerCourseClass();
                entity.setClassSection(cs);
                entity.setLecturer(lecturer);
                entity.setNote(note != null ? note.trim() : null);
                repository.save(entity);
            }
        }
    }

    /**
     * Phân công hiện có của giảng viên (một nguồn dữ liệu — không nhân bản bảng khác).
     */
    @Transactional(readOnly = true)
    public List<LecturerTeachingAssignmentRow> listTeachingRowsForLecturer(UUID lecturerId) {
        return repository.findAllWithSectionGraphByLecturerId(lecturerId).stream()
                .map(lcc -> {
                    ClassSection cs = lcc.getClassSection();
                    ClassEntity ac = cs != null ? cs.getAdministrativeClass() : null;
                    String courseCode = null;
                    String courseName = null;
                    if (cs != null && cs.getCourse() != null) {
                        courseCode = cs.getCourse().getCourseCode();
                        courseName = cs.getCourse().getCourseName();
                    }
                    String semesterCode = (cs != null && cs.getSemester() != null)
                            ? cs.getSemester().getCode()
                            : null;
                    return new LecturerTeachingAssignmentRow(
                            lcc.getId(),
                            cs != null ? cs.getId() : null,
                            cs != null ? cs.getClassCode() : null,
                            cs != null ? cs.getClassName() : null,
                            courseCode,
                            courseName,
                            semesterCode,
                            ac != null ? ac.getClassId() : null,
                            ac != null ? ac.getClassCode() : null,
                            ac != null ? ac.getClassName() : null
                    );
                })
                .toList();
    }

    /** Giảng viên có lớp học phần gắn {@link ClassEntity} này (qua administrative_class). */
    @Transactional(readOnly = true)
    public List<CohortTeachingLecturerRow> listTeachingLecturersForAdministrativeClass(UUID administrativeClassId) {
        return repository.findAllWithLecturerByAdministrativeClassId(administrativeClassId).stream()
                .map(LecturerCourseClass::getLecturer)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Lecturer::getLecturerId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparing(Lecturer::getLecturerCode,
                        Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(l -> new CohortTeachingLecturerRow(
                        l.getLecturerId(),
                        l.getLecturerCode(),
                        l.getFullName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("LecturerCourseClasses");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Class Code");
            header.createCell(1).setCellValue("Class Name");
            header.createCell(2).setCellValue("Course Code");
            header.createCell(3).setCellValue("Course Name");
            header.createCell(4).setCellValue("Semester Code");
            header.createCell(5).setCellValue("Administrative class code");
            header.createCell(6).setCellValue("Administrative class name");
            header.createCell(7).setCellValue("Lecturer Code");
            header.createCell(8).setCellValue("Lecturer Name");
            header.createCell(9).setCellValue("Faculty");
            header.createCell(10).setCellValue("Note");

            List<LecturerCourseClass> list = repository.findAll(
                    Sort.by(Sort.Direction.ASC, "classSection.classCode")
                            .and(Sort.by("lecturer.lecturerCode"))
            );
            int rowNum = 1;
            for (LecturerCourseClass e : list) {
                Row row = sheet.createRow(rowNum++);
                ClassSection cs = e.getClassSection();
                Lecturer l = e.getLecturer();
                row.createCell(0).setCellValue(cs != null ? nullToEmpty(cs.getClassCode()) : "");
                row.createCell(1).setCellValue(cs != null ? nullToEmpty(cs.getClassName()) : "");
                row.createCell(2).setCellValue(cs != null && cs.getCourse() != null ? nullToEmpty(cs.getCourse().getCourseCode()) : "");
                row.createCell(3).setCellValue(cs != null && cs.getCourse() != null ? nullToEmpty(cs.getCourse().getCourseName()) : "");
                row.createCell(4).setCellValue(cs != null && cs.getSemester() != null ? nullToEmpty(cs.getSemester().getCode()) : "");
                ClassEntity ac = cs != null ? cs.getAdministrativeClass() : null;
                row.createCell(5).setCellValue(ac != null ? nullToEmpty(ac.getClassCode()) : "");
                row.createCell(6).setCellValue(ac != null ? nullToEmpty(ac.getClassName()) : "");
                row.createCell(7).setCellValue(l != null ? nullToEmpty(l.getLecturerCode()) : "");
                row.createCell(8).setCellValue(l != null ? nullToEmpty(l.getFullName()) : "");
                row.createCell(9).setCellValue(l != null && l.getFaculty() != null ? nullToEmpty(l.getFaculty().getFacultyName()) : "");
                row.createCell(10).setCellValue(nullToEmpty(e.getNote()));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void validateDuplicate(Long classSectionId, UUID lecturerId, Long currentId) {
        boolean exists = (currentId == null)
                ? repository.existsByClassSection_IdAndLecturer_LecturerId(classSectionId, lecturerId)
                : repository.existsByClassSection_IdAndLecturer_LecturerIdAndIdNot(classSectionId, lecturerId, currentId);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Giảng viên đã được phân công cho lớp học phần này");
        }
    }

    /**
     * Nếu lớp học phần chưa gắn lớp hành chính, thử khớp theo mã lớp + năm học của học kỳ (trùng với lớp trong QLĐT).
     */
    private void ensureAdministrativeClassLinked(ClassSection cs) {
        if (cs == null || cs.getAdministrativeClass() != null) {
            return;
        }
        if (cs.getSemester() == null || cs.getClassCode() == null || cs.getClassCode().isBlank()) {
            return;
        }
        String year = cs.getSemester().getAcademicYear();
        if (year == null || year.isBlank()) {
            return;
        }
        classRepository.findByClassCodeIgnoreCaseAndAcademicYear(cs.getClassCode(), year)
                .ifPresent(ac -> {
                    cs.setAdministrativeClass(ac);
                    classSectionRepository.save(cs);
                });
    }

    private LecturerCourseClass build(LecturerCourseClass entity, LecturerCourseClassRequest req) {
        ClassSection cs = classSectionRepository.findById(req.getClassSectionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
        ensureAdministrativeClassLinked(cs);
        Lecturer lecturer = lecturerRepository.findById(req.getLecturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên"));

        entity.setClassSection(cs);
        entity.setLecturer(lecturer);
        entity.setNote(req.getNote() != null ? req.getNote().trim() : null);
        return entity;
    }

    private LecturerCourseClassResponse toResponse(LecturerCourseClass e) {
        ClassSection cs = e.getClassSection();
        Lecturer l = e.getLecturer();
        String courseCode = null;
        String courseName = null;
        if (cs != null && cs.getCourse() != null) {
            courseCode = cs.getCourse().getCourseCode();
            courseName = cs.getCourse().getCourseName();
        }
        String semesterCode = (cs != null && cs.getSemester() != null)
                ? cs.getSemester().getCode()
                : null;
        String facultyName = (l != null && l.getFaculty() != null)
                ? l.getFaculty().getFacultyName()
                : null;
        ClassEntity ac = cs != null ? cs.getAdministrativeClass() : null;
        return new LecturerCourseClassResponse(
                e.getId(),
                cs != null ? cs.getId() : null,
                cs != null ? cs.getClassCode() : null,
                cs != null ? cs.getClassName() : null,
                courseCode,
                courseName,
                semesterCode,
                ac != null ? ac.getClassId() : null,
                ac != null ? ac.getClassCode() : null,
                ac != null ? ac.getClassName() : null,
                l != null ? l.getLecturerId() : null,
                l != null ? l.getLecturerCode() : null,
                l != null ? l.getFullName() : null,
                facultyName,
                e.getNote(),
                e.getCreatedAt(),
                e.getUpdatedAt()
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


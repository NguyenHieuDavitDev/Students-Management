package com.example.stduents_management.classsection.service;

import com.example.stduents_management.classsection.dto.ClassSectionRequest;
import com.example.stduents_management.classsection.dto.ClassSectionResponse;
import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.entity.ClassSectionStatus;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.semester.entity.Semester;
import com.example.stduents_management.semester.repository.SemesterRepository;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassSectionService {

    private final ClassSectionRepository repository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final RoomRepository roomRepository;

    public Page<ClassSectionResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "classCode"));
        Page<ClassSection> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository.searchByClassCodeOrClassNameOrCourseOrSemester(keyword, pageable);
        return data.map(this::toResponse);
    }

    public ClassSectionResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
    }

    @Transactional
    public void create(ClassSectionRequest req) {
        validateBusinessRules(req);
        if (repository.existsByClassCode(req.getClassCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã lớp đã tồn tại");
        }
        repository.save(build(new ClassSection(), req));
    }

    @Transactional
    public void update(Long id, ClassSectionRequest req) {
        ClassSection cs = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
        validateBusinessRules(req);
        if (repository.existsByClassCodeAndIdNot(req.getClassCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã lớp đã tồn tại");
        }
        build(cs, req);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần");
        }
        repository.deleteById(id);
    }

    public List<ClassSectionResponse> getForPrint() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "classCode"))
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

                String classCode = readString(row, 0, formatter);
                if (classCode == null || classCode.isBlank()) continue;
                if (repository.existsByClassCode(classCode.trim())) continue;

                String courseCode = readString(row, 1, formatter);
                String semesterCode = readString(row, 2, formatter);
                String className = readString(row, 3, formatter);
                Integer maxStudents = readInteger(row, 4, formatter);
                Integer currentStudents = readInteger(row, 5, formatter);
                String statusRaw = readString(row, 6, formatter);
                String room = readString(row, 7, formatter);
                String note = readString(row, 8, formatter);

                if (courseCode == null || courseCode.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Mã môn học không được để trống");
                }
                if (semesterCode == null || semesterCode.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Mã học kỳ không được để trống");
                }
                if (className == null || className.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Tên lớp không được để trống");
                }

                Course course = courseRepository.findByCourseCodeIgnoreCase(courseCode)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy môn học " + courseCode));

                Semester semester = semesterRepository.findByCodeIgnoreCase(semesterCode)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy học kỳ " + semesterCode));

                ClassSectionStatus status = ClassSectionStatus.OPEN;
                if (statusRaw != null && !statusRaw.isBlank()) {
                    try {
                        status = ClassSectionStatus.valueOf(statusRaw.trim().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Dòng " + rowNum + ": Status không hợp lệ (OPEN, CLOSED, CANCELLED)");
                    }
                }

                int max = maxStudents != null ? maxStudents : 50;
                int current = currentStudents != null ? currentStudents : 0;
                if (current > max) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Sĩ số hiện tại không được lớn hơn sĩ số tối đa");
                }

                Room roomEntity = null;
                if (room != null && !room.isBlank()) {
                    roomEntity = roomRepository.findByRoomCodeIgnoreCase(room.trim()).orElse(null);
                }

                ClassSection cs = new ClassSection();
                cs.setClassCode(classCode.trim());
                cs.setClassName(className.trim());
                cs.setCourse(course);
                cs.setSemester(semester);
                cs.setMaxStudents(max);
                cs.setCurrentStudents(current);
                cs.setStatus(status);
                cs.setRoom(roomEntity);
                cs.setNote(note != null ? note.trim() : null);
                repository.save(cs);
            }
        }
    }

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("ClassSections");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Class Code");
            header.createCell(1).setCellValue("Course Code");
            header.createCell(2).setCellValue("Semester Code");
            header.createCell(3).setCellValue("Class Name");
            header.createCell(4).setCellValue("Max Students");
            header.createCell(5).setCellValue("Current Students");
            header.createCell(6).setCellValue("Status");
            header.createCell(7).setCellValue("Room Code");
            header.createCell(8).setCellValue("Note");

            List<ClassSection> list = repository.findAll(Sort.by(Sort.Direction.ASC, "classCode"));
            int rowNum = 1;
            for (ClassSection cs : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nullToEmpty(cs.getClassCode()));
                row.createCell(1).setCellValue(cs.getCourse() != null ? nullToEmpty(cs.getCourse().getCourseCode()) : "");
                row.createCell(2).setCellValue(cs.getSemester() != null ? nullToEmpty(cs.getSemester().getCode()) : "");
                row.createCell(3).setCellValue(nullToEmpty(cs.getClassName()));
                row.createCell(4).setCellValue(cs.getMaxStudents() != null ? cs.getMaxStudents() : 0);
                row.createCell(5).setCellValue(cs.getCurrentStudents() != null ? cs.getCurrentStudents() : 0);
                row.createCell(6).setCellValue(cs.getStatus() != null ? cs.getStatus().name() : "");
                row.createCell(7).setCellValue(cs.getRoom() != null ? nullToEmpty(cs.getRoom().getRoomCode()) : "");
                row.createCell(8).setCellValue(nullToEmpty(cs.getNote()));
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void validateBusinessRules(ClassSectionRequest req) {
        Integer max = req.getMaxStudents();
        Integer current = req.getCurrentStudents();
        if (max != null && current != null && current > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sĩ số hiện tại không được lớn hơn sĩ số tối đa");
        }
    }

    private ClassSection build(ClassSection cs, ClassSectionRequest req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy môn học"));
        Semester semester = semesterRepository.findById(req.getSemesterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ"));

        Room room = null;
        if (req.getRoomId() != null) {
            room = roomRepository.findById(req.getRoomId())
                    .orElse(null);
        }

        cs.setCourse(course);
        cs.setSemester(semester);
        cs.setClassCode(req.getClassCode() != null ? req.getClassCode().trim() : "");
        cs.setClassName(req.getClassName() != null ? req.getClassName().trim() : "");
        cs.setMaxStudents(req.getMaxStudents());
        cs.setCurrentStudents(req.getCurrentStudents() != null ? req.getCurrentStudents() : 0);
        cs.setStatus(req.getStatus() != null ? req.getStatus() : ClassSectionStatus.OPEN);
        cs.setRoom(room);
        cs.setNote(req.getNote() != null ? req.getNote().trim() : null);
        return cs;
    }

    private ClassSectionResponse toResponse(ClassSection cs) {
        Course c = cs.getCourse();
        Semester s = cs.getSemester();
        Room r = cs.getRoom();
        return new ClassSectionResponse(
                cs.getId(),
                c != null ? c.getId() : null,
                c != null ? c.getCourseCode() : null,
                c != null ? c.getCourseName() : null,
                s != null ? s.getId() : null,
                s != null ? s.getCode() : null,
                s != null ? s.getName() : null,
                cs.getClassCode(),
                cs.getClassName(),
                cs.getMaxStudents(),
                cs.getCurrentStudents(),
                cs.getStatus(),
                r != null ? r.getRoomId() : null,
                r != null ? r.getRoomCode() : null,
                r != null ? r.getRoomName() : null,
                cs.getNote(),
                cs.getCreatedAt(),
                cs.getUpdatedAt()
        );
    }

    private String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }

    private Integer readInteger(Row row, int cellIndex, DataFormatter formatter) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}

package com.example.stduents_management.attendance.service;

import com.example.stduents_management.attendance.dto.AttendanceRequest;
import com.example.stduents_management.attendance.dto.AttendanceResponse;
import com.example.stduents_management.attendance.entity.StudentAttendance;
import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.courseregistration.entity.CourseRegistration;
import com.example.stduents_management.courseregistration.repository.CourseRegistrationRepository;
import com.example.stduents_management.attendance.repository.StudentAttendanceRepository;
import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.student.repository.StudentRepository;
import com.example.stduents_management.lecturercourseclass.repository.LecturerCourseClassRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final StudentAttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final ClassSectionRepository classSectionRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final LecturerCourseClassRepository lecturerCourseClassRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> search(
            String keyword,
            Long courseClassId,
            LocalDate attendanceDate,
            int page,
            int size
    ) {
        String kw = normalize(keyword);
        String kwOrNull = (kw == null || kw.isBlank()) ? null : kw;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return attendanceRepository
                .search(kwOrNull, courseClassId, attendanceDate, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getById(UUID id) {
        return attendanceRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi điểm danh"
                ));
    }

    @Transactional
    public AttendanceResponse upsert(UUID idOrNullIfCreate, AttendanceRequest req, Optional<UUID> markedByLecturerId) {
        if (!courseRegistrationRepository.existsByStudent_StudentIdAndClassSection_Id(
                req.getStudentId(),
                req.getCourseClassId()
        )) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sinh viên chưa đăng ký lớp học phần này");
        }

        Lecturer markedBy = null;
        if (markedByLecturerId.isPresent()) {
            UUID lecturerId = markedByLecturerId.get();
            if (!lecturerCourseClassRepository.existsByClassSection_IdAndLecturer_LecturerId(
                    req.getCourseClassId(), lecturerId
            )) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Giảng viên không được phép điểm danh lớp học phần này");
            }
            markedBy = lecturerRepository.findById(lecturerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên"));
        }

        ClassSection classSection = classSectionRepository.findById(req.getCourseClassId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên"));

        StudentAttendance entity;
        if (idOrNullIfCreate != null) {
            entity = attendanceRepository.findById(idOrNullIfCreate)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi điểm danh"));
        } else {
            entity = attendanceRepository.findByStudent_StudentIdAndCourseClass_IdAndAttendanceDate(
                            req.getStudentId(),
                            req.getCourseClassId(),
                            req.getAttendanceDate()
                    )
                    .orElseGet(StudentAttendance::new);
        }

        // Nếu đang "upsert theo khóa" (create hoặc record chưa tồn tại), gán các quan hệ.
        if (entity.getAttendanceId() == null) {
            entity.setStudent(student);
            entity.setCourseClass(classSection);
            entity.setAttendanceDate(req.getAttendanceDate());
        }

        entity.setPresent(req.getPresent());
        entity.setNote(req.getNote() != null && !req.getNote().isBlank() ? req.getNote().trim() : null);
        entity.setMarkedBy(markedBy);
        entity.setMarkedAt(LocalDateTime.now());

        return toResponse(attendanceRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi điểm danh");
        }
        attendanceRepository.deleteById(id);
    }

    /**
     * Khởi tạo bản ghi điểm danh cho mọi sinh viên đã đăng ký trong lớp học phần tại ngày đã chọn.
     * Mặc định present = false (chờ giảng viên cập nhật).
     */
    @Transactional
    public void initializeForClassAndDate(
            Long courseClassId,
            LocalDate attendanceDate,
            Optional<UUID> markedByLecturerId
    ) {
        ClassSection classSection = classSectionRepository.findById(courseClassId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));

        Lecturer markedBy = null;
        if (markedByLecturerId.isPresent()) {
            UUID lecturerId = markedByLecturerId.get();
            if (!lecturerCourseClassRepository.existsByClassSection_IdAndLecturer_LecturerId(courseClassId, lecturerId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Giảng viên không được phép điểm danh lớp học phần này");
            }
            markedBy = lecturerRepository.findById(lecturerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên"));
        }

        List<CourseRegistration> registrations = courseRegistrationRepository
                .findByClassSection_IdOrderByStudent_FullName(courseClassId);

        if (registrations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lớp học phần không có sinh viên đăng ký");
        }

        LocalDateTime now = LocalDateTime.now();
        List<StudentAttendance> toSave = new ArrayList<>();
        for (CourseRegistration cr : registrations) {
            UUID studentId = cr.getStudent().getStudentId();
            boolean exists = attendanceRepository.existsByStudent_StudentIdAndCourseClass_IdAndAttendanceDate(
                    studentId, courseClassId, attendanceDate
            );
            if (exists) continue;

            StudentAttendance a = new StudentAttendance();
            a.setStudent(cr.getStudent());
            a.setCourseClass(classSection);
            a.setAttendanceDate(attendanceDate);
            a.setPresent(Boolean.FALSE);
            a.setNote(null);
            a.setMarkedBy(markedBy);
            a.setMarkedAt(now);
            toSave.add(a);
        }

        if (!toSave.isEmpty()) {
            attendanceRepository.saveAll(toSave);
        }
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getForPrint(Long courseClassId, LocalDate attendanceDate) {
        List<CourseRegistration> registrations =
                courseRegistrationRepository.findByClassSection_IdOrderByStudent_FullName(courseClassId);

        List<StudentAttendance> attendances =
                attendanceRepository.findAllByCourseClassIdAndAttendanceDate(courseClassId, attendanceDate);

        Map<UUID, StudentAttendance> attendanceByStudentId = new HashMap<>();
        for (StudentAttendance a : attendances) {
            attendanceByStudentId.put(a.getStudent().getStudentId(), a);
        }

        ClassSection classSection = classSectionRepository.findById(courseClassId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));

        Course course = classSection.getCourse();

        List<AttendanceResponse> result = new ArrayList<>();
        for (CourseRegistration cr : registrations) {
            StudentAttendance a = attendanceByStudentId.get(cr.getStudent().getStudentId());
            if (a != null) {
                result.add(toResponse(a));
            } else {
                result.add(new AttendanceResponse(
                        null,
                        cr.getStudent().getStudentId(),
                        cr.getStudent().getStudentCode(),
                        cr.getStudent().getFullName(),
                        courseClassId,
                        classSection.getClassCode(),
                        classSection.getClassName(),
                        course != null ? course.getCourseCode() : null,
                        course != null ? course.getCourseName() : null,
                        attendanceDate,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public void exportExcel(HttpServletResponse response, Long courseClassId, LocalDate attendanceDate) {
        try {
            List<AttendanceResponse> list = getForPrint(courseClassId, attendanceDate);

            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("Điểm danh");
                int[] widths = {4500, 12000, 6000, 12000, 6000, 3500, 7000, 9000, 6000, 6000};
                for (int i = 0; i < widths.length; i++) {
                    sheet.setColumnWidth(i, widths[i]);
                }

                CellStyle titleStyle = wb.createCellStyle();
                Font titleFont = wb.createFont();
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 14);
                titleStyle.setFont(titleFont);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);

                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue("DANH SÁCH ĐIỂM DANH SINH VIÊN");
                titleCell.setCellStyle(titleStyle);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

                CellStyle headerStyle = headerStyle(wb);
                Row header = sheet.createRow(1);
                String[] headers = {
                        "Mã SV",
                        "Họ tên",
                        "Mã lớp HP",
                        "Lớp HP",
                        "Học phần",
                        "Ngày",
                        "Trạng thái",
                        "Ghi chú",
                        "GV điểm danh",
                        "Thời gian"
                };
                for (int i = 0; i < headers.length; i++) {
                    Cell c = header.createCell(i);
                    c.setCellValue(headers[i]);
                    c.setCellStyle(headerStyle);
                }

                CellStyle dataStyle = dataStyle(wb);
                int rowIdx = 2;
                for (AttendanceResponse a : list) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(a.studentCode() != null ? a.studentCode() : "");
                    row.createCell(1).setCellValue(a.studentName() != null ? a.studentName() : "");
                    row.createCell(2).setCellValue(a.classCode() != null ? a.classCode() : "");
                    row.createCell(3).setCellValue(a.className() != null ? a.className() : "");
                    row.createCell(4).setCellValue(
                            (a.courseCode() != null ? a.courseCode() : "") + (a.courseName() != null ? " - " + a.courseName() : "")
                    );
                    row.createCell(5).setCellValue(a.attendanceDate() != null ? a.attendanceDate().format(DATE_FMT) : "");
                    row.createCell(6).setCellValue(toStatusLabel(a.present()));
                    row.createCell(7).setCellValue(a.note() != null ? a.note() : "");
                    row.createCell(8).setCellValue(a.lecturerName() != null ? a.lecturerName() : "");
                    row.createCell(9).setCellValue(a.markedAt() != null ? a.markedAt().format(DATETIME_FMT) : "");

                    for (int c = 0; c < 10; c++) {
                        row.getCell(c).setCellStyle(dataStyle);
                    }
                }

                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=attendances.xlsx");
                wb.write(response.getOutputStream());
                response.flushBuffer();
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể xuất Excel: " + e.getMessage());
        }
    }

    @Transactional
    public int importExcel(
            MultipartFile file,
            Long courseClassId,
            LocalDate attendanceDate,
            Optional<UUID> defaultMarkedByLecturerId
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            DataFormatter formatter = new DataFormatter(Locale.getDefault());

            Sheet sheet = wb.getSheetAt(0);
            int count = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String studentCode = getCellString(row.getCell(0), formatter);
                    if (studentCode == null || studentCode.isBlank()) continue;

                    String presentStr = getCellString(row.getCell(1), formatter);
                    if (presentStr == null || presentStr.isBlank()) continue;

                    Boolean present = parsePresent(presentStr);
                    if (present == null) continue;

                    String note = getCellString(row.getCell(2), formatter);
                    if (note != null && note.isBlank()) note = null;

                    String lecturerCode = getCellString(row.getCell(3), formatter);
                    Optional<UUID> lecturerIdToUse = Optional.empty();
                    if (lecturerCode != null && !lecturerCode.isBlank()) {
                        lecturerIdToUse = lecturerRepository.findByLecturerCodeIgnoreCase(lecturerCode.trim())
                                .map(Lecturer::getLecturerId);
                    } else {
                        lecturerIdToUse = defaultMarkedByLecturerId;
                    }

                    Student student = studentRepository.findByStudentCodeIgnoreCase(studentCode.trim())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Không tìm thấy sinh viên: " + studentCode
                            ));

                    if (!courseRegistrationRepository.existsByStudent_StudentIdAndClassSection_Id(
                            student.getStudentId(), courseClassId
                    )) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Sinh viên " + studentCode + " chưa đăng ký lớp học phần này");
                    }

                    AttendanceRequest req = new AttendanceRequest();
                    req.setStudentId(student.getStudentId());
                    req.setCourseClassId(courseClassId);
                    req.setAttendanceDate(attendanceDate);
                    req.setPresent(present);
                    req.setNote(note);
                    req.setMarkedByLecturerId(lecturerIdToUse.orElse(null));

                    AttendanceResponse saved = upsert(null, req, lecturerIdToUse);
                    if (saved != null) count++;
                } catch (Exception ignore) {
                    // bỏ qua dòng lỗi
                }
            }
            return count;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File Excel không hợp lệ: " + e.getMessage());
        }
    }

    public byte[] getImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Mẫu nhập điểm danh");
            Row header = sheet.createRow(0);
            String[] headers = {"Mã SV", "Có mặt (1/0)", "Ghi chú", "Mã GV (tùy chọn)"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("SV001");
            example.createCell(1).setCellValue(1);
            example.createCell(2).setCellValue("Có mặt đúng giờ");
            example.createCell(3).setCellValue("GV001");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể tạo mẫu Excel: " + e.getMessage());
        }
    }

    // ----------------- Helpers -----------------

    private AttendanceResponse toResponse(StudentAttendance a) {
        ClassSection cs = a.getCourseClass();
        Course course = cs != null ? cs.getCourse() : null;
        Lecturer lecturer = a.getMarkedBy();

        return new AttendanceResponse(
                a.getAttendanceId(),
                a.getStudent() != null ? a.getStudent().getStudentId() : null,
                a.getStudent() != null ? a.getStudent().getStudentCode() : null,
                a.getStudent() != null ? a.getStudent().getFullName() : null,
                cs != null ? cs.getId() : null,
                cs != null ? cs.getClassCode() : null,
                cs != null ? cs.getClassName() : null,
                course != null ? course.getCourseCode() : null,
                course != null ? course.getCourseName() : null,
                a.getAttendanceDate(),
                a.getPresent(),
                a.getNote(),
                lecturer != null ? lecturer.getLecturerId() : null,
                lecturer != null ? lecturer.getLecturerCode() : null,
                lecturer != null ? lecturer.getFullName() : null,
                a.getMarkedAt()
        );
    }

    private static String normalize(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim();
    }

    private static CellStyle headerStyle(Workbook wb) {
        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        return headerStyle;
    }

    private static CellStyle dataStyle(Workbook wb) {
        CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        return dataStyle;
    }

    private static String toStatusLabel(Boolean present) {
        if (present == null) return "";
        return present ? "Có mặt" : "Vắng";
    }

    private static String getCellString(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        String s = formatter.formatCellValue(cell);
        return s != null ? s.trim() : null;
    }

    /**
     * Trả về true/false nếu parse được, ngược lại null.
     */
    private static Boolean parsePresent(String s) {
        if (s == null) return null;
        String raw = s.trim().toLowerCase(Locale.ROOT);

        if (raw.matches("^[0-9]+$")) {
            int v = Integer.parseInt(raw);
            if (v == 0) return false;
            if (v == 1) return true;
        }

        // Normalize: bỏ dấu tiếng Việt + bỏ khoảng trắng để so khớp ổn định.
        String normalized = java.text.Normalizer.normalize(raw, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("\\s+", "");

        if (normalized.equals("true") || normalized.equals("yes") || normalized.equals("y") || normalized.equals("comat")) {
            return true;
        }
        if (normalized.equals("false") || normalized.equals("no") || normalized.equals("n") || normalized.equals("vang") || normalized.equals("vanga")) {
            return false;
        }

        if (normalized.equals("1") || normalized.equals("0")) {
            return normalized.equals("1");
        }
        return null;
    }

    public static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception ignore) {
            // continue
        }
        try {
            return LocalDate.parse(s.trim(), DATE_FMT);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày không hợp lệ: " + s);
        }
    }
}


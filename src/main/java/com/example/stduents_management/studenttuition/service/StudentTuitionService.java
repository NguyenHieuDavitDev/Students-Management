package com.example.stduents_management.studenttuition.service;

import com.example.stduents_management.semester.entity.Semester;
import com.example.stduents_management.semester.repository.SemesterRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.tuitionfee.entity.TuitionFee;
import com.example.stduents_management.tuitionfee.repository.TuitionFeeRepository;
import com.example.stduents_management.student.repository.StudentRepository;
import com.example.stduents_management.studenttuition.dto.StudentTuitionRequest;
import com.example.stduents_management.studenttuition.dto.StudentTuitionResponse;
import com.example.stduents_management.studenttuition.entity.StudentTuition;
import com.example.stduents_management.studenttuition.entity.StudentTuitionStatus;
import com.example.stduents_management.studenttuition.repository.StudentTuitionRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentTuitionService {

    private final StudentTuitionRepository studentTuitionRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final TuitionFeeRepository tuitionFeeRepository;

    // ─── SEARCH / LIST ────────────────────────────────────────────────────────

    public Page<StudentTuitionResponse> search(String keyword, String statusStr, int page, int size) {
        StudentTuitionStatus status = parseStatus(statusStr);
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size);
        return studentTuitionRepository.search(term, status, pageable).map(this::toResponse);
    }

    public StudentTuitionResponse getById(UUID id) {
        return studentTuitionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy học phí sinh viên"));
    }

    public List<StudentTuitionResponse> getAll() {
        return studentTuitionRepository.findAllOrdered().stream().map(this::toResponse).toList();
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    public void create(StudentTuitionRequest req) {
        Student student = resolveStudent(req.getStudentId());
        Semester semester = resolveSemester(req.getSemesterId());

        // Nếu tổng học phí chưa được nhập, thử tự tính từ cấu hình học phí (tuition_fees)
        if (req.getTotalAmount() == null && req.getTotalCredits() != null) {
            BigDecimal autoAmount = calculateTuitionAmountForStudent(student, semester, req.getTotalCredits());
            req.setTotalAmount(autoAmount);
        }

        StudentTuition entity = new StudentTuition();
        buildEntity(entity, req, student, semester);
        studentTuitionRepository.save(entity);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public void update(UUID id, StudentTuitionRequest req) {
        StudentTuition entity = studentTuitionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy học phí sinh viên"));

        Student student = resolveStudent(req.getStudentId());
        Semester semester = resolveSemester(req.getSemesterId());

        if (req.getTotalAmount() == null && req.getTotalCredits() != null) {
            BigDecimal autoAmount = calculateTuitionAmountForStudent(student, semester, req.getTotalCredits());
            req.setTotalAmount(autoAmount);
        }

        buildEntity(entity, req, student, semester);
        studentTuitionRepository.save(entity);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Transactional
    public void delete(UUID id) {
        if (!studentTuitionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phí sinh viên");
        }
        studentTuitionRepository.deleteById(id);
    }

    // ─── EXPORT EXCEL ─────────────────────────────────────────────────────────

    public void exportExcel(HttpServletResponse response) {
        List<StudentTuition> all = studentTuitionRepository.findAllOrdered();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Học phí sinh viên");
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 8000);
            sheet.setColumnWidth(2, 5000);
            sheet.setColumnWidth(3, 8000);
            sheet.setColumnWidth(4, 4000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 5000);
            sheet.setColumnWidth(7, 5000);
            sheet.setColumnWidth(8, 5000);

            // Title row
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BẢNG HỌC PHÍ SINH VIÊN THEO HỌC KỲ");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Header row
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Row header = sheet.createRow(1);
            String[] headers = {
                    "Mã SV", "Họ tên", "Học kỳ", "Năm học",
                    "Tổng tín chỉ", "Tổng học phí", "Đã đóng", "Còn thiếu", "Trạng thái"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            // Data rows
            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowIdx = 2;
            for (StudentTuition st : all) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(st.getStudent().getStudentCode());
                row.createCell(1).setCellValue(st.getStudent().getFullName());
                row.createCell(2).setCellValue(st.getSemester().getCode());
                row.createCell(3).setCellValue(st.getSemester().getAcademicYear());
                row.createCell(4).setCellValue(st.getTotalCredits());
                row.createCell(5).setCellValue(st.getTotalAmount().toPlainString());
                row.createCell(6).setCellValue(st.getAmountPaid().toPlainString());
                row.createCell(7).setCellValue(st.getRemainingAmount().toPlainString());
                row.createCell(8).setCellValue(st.getStatus().getLabel());
                for (int c = 0; c < 9; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=student-tuition.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể xuất file Excel: " + e.getMessage());
        }
    }

    // ─── IMPORT EXCEL ─────────────────────────────────────────────────────────

    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }

        int count = 0;
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            // Row 0: title, Row 1: header → data starts at row 2
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String studentCode  = getCellString(row.getCell(0));
                    String semesterCode = getCellString(row.getCell(2));
                    String academicYear = getCellString(row.getCell(3));
                    String creditsStr   = getCellString(row.getCell(4));
                    String totalStr     = getCellString(row.getCell(5));
                    String paidStr      = getCellString(row.getCell(6));
                    String statusStr    = getCellString(row.getCell(8));

                    if (studentCode.isBlank() || semesterCode.isBlank() || academicYear.isBlank()) {
                        continue;
                    }

                    Student student = studentRepository.findByStudentCode(studentCode)
                            .orElse(null);
                    if (student == null) continue;

                    Semester semester = semesterRepository
                            .findByCodeAndAcademicYear(semesterCode, academicYear)
                            .orElse(null);
                    if (semester == null) continue;

                    Integer credits;
                    try {
                        credits = Integer.parseInt(creditsStr);
                    } catch (NumberFormatException ex) {
                        continue;
                    }

                    BigDecimal totalAmount = parseMoney(totalStr);
                    BigDecimal amountPaid  = parseMoney(paidStr);
                    if (totalAmount == null || amountPaid == null) continue;

                    StudentTuitionStatus status = StudentTuitionStatus.UNPAID;
                    if (statusStr.equalsIgnoreCase("PAID") ||
                            statusStr.equalsIgnoreCase("Đã đóng đủ")) {
                        status = StudentTuitionStatus.PAID;
                    } else if (statusStr.equalsIgnoreCase("PARTIAL") ||
                            statusStr.equalsIgnoreCase("Đã đóng một phần")) {
                        status = StudentTuitionStatus.PARTIAL;
                    }

                    StudentTuition st = new StudentTuition();
                    st.setStudent(student);
                    st.setSemester(semester);
                    st.setTotalCredits(credits);
                    st.setTotalAmount(totalAmount);
                    st.setAmountPaid(amountPaid);
                    st.setStatus(status);
                    // remainingAmount & timestamps handled by entity callbacks
                    studentTuitionRepository.save(st);
                    count++;

                } catch (Exception e) {
                    // Bỏ qua dòng lỗi, tiếp tục dòng tiếp theo
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ: " + e.getMessage());
        }
        return count;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private Student resolveStudent(UUID studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy sinh viên"));
    }

    private Semester resolveSemester(Long semesterId) {
        return semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy học kỳ"));
    }

    private void buildEntity(StudentTuition entity,
                             StudentTuitionRequest req,
                             Student student,
                             Semester semester) {
        entity.setStudent(student);
        entity.setSemester(semester);
        entity.setTotalCredits(req.getTotalCredits());
        entity.setTotalAmount(req.getTotalAmount());
        entity.setAmountPaid(req.getAmountPaid());

        BigDecimal remaining = req.getTotalAmount().subtract(req.getAmountPaid());
        entity.setRemainingAmount(remaining);

        StudentTuitionStatus status = req.getStatus();
        if (status == null) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                status = StudentTuitionStatus.PAID;
            } else if (req.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
                status = StudentTuitionStatus.PARTIAL;
            } else {
                status = StudentTuitionStatus.UNPAID;
            }
        }
        entity.setStatus(status);
    }

    /**
     * Tính tổng học phí cho sinh viên dựa trên:
     * - Số tín chỉ đăng ký
     * - Mức học phí/tín chỉ ACTIVE của chương trình đào tạo tương ứng
     *
     * Logic:
     * - Xác định ngành (major) từ lớp của sinh viên.
     * - Tìm chương trình đào tạo (training_programs) phù hợp với ngành đó và khóa học (course) gần nhất.
     * - Lấy mức học phí ACTIVE mới nhất của chương trình đó.
     * - Nhân với tổng số tín chỉ để ra tổng học phí.
     *
     * Ghi chú: Nếu không tìm thấy dữ liệu phù hợp, trả về 0 để tránh lỗi.
     */
    public BigDecimal calculateTuitionAmountForStudent(Student student, Semester semester, Integer totalCredits) {
        if (student == null || totalCredits == null || totalCredits <= 0) {
            return BigDecimal.ZERO;
        }

        var clazz = student.getClazz();
        if (clazz == null || clazz.getMajor() == null) {
            return BigDecimal.ZERO;
        }

        var major = clazz.getMajor();

        // Tìm tất cả chương trình của ngành này, chọn chương trình đang ACTIVE nếu có
        List<TuitionFee> allFees = tuitionFeeRepository.findAllOrdered();
        TuitionFee activeFee = allFees.stream()
                .filter(tf -> tf.getTrainingProgram() != null
                        && tf.getTrainingProgram().getMajor() != null
                        && tf.getTrainingProgram().getMajor().getMajorId().equals(major.getMajorId())
                        && tf.getStatus() == com.example.stduents_management.tuitionfee.entity.TuitionFeeStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        if (activeFee == null || activeFee.getFeePerCredit() == null) {
            return BigDecimal.ZERO;
        }

        return activeFee.getFeePerCredit().multiply(BigDecimal.valueOf(totalCredits.longValue()));
    }

    public StudentTuitionResponse toResponse(StudentTuition st) {
        Student s = st.getStudent();
        Semester sem = st.getSemester();
        return new StudentTuitionResponse(
                st.getId(),
                s.getStudentId(),
                s.getStudentCode(),
                s.getFullName(),
                sem.getId(),
                sem.getCode(),
                sem.getName(),
                st.getTotalCredits(),
                st.getTotalAmount(),
                st.getAmountPaid(),
                st.getRemainingAmount(),
                st.getStatus(),
                st.getStatus().getLabel(),
                st.getCreatedAt(),
                st.getUpdatedAt()
        );
    }

    private StudentTuitionStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return StudentTuitionStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private BigDecimal parseMoney(String s) {
        if (s == null) return null;
        String cleaned = s.replaceAll("[^0-9.]", "");
        if (cleaned.isBlank()) return null;
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}


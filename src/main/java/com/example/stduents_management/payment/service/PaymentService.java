package com.example.stduents_management.payment.service;

import com.example.stduents_management.payment.dto.PaymentRequest;
import com.example.stduents_management.payment.dto.PaymentResponse;
import com.example.stduents_management.payment.entity.Payment;
import com.example.stduents_management.payment.entity.PaymentMethod;
import com.example.stduents_management.payment.entity.PaymentStatus;
import com.example.stduents_management.payment.repository.PaymentRepository;
import com.example.stduents_management.notification.entity.NotificationCategory;
import com.example.stduents_management.notification.service.NotificationService;
import com.example.stduents_management.studenttuition.entity.StudentTuition;
import com.example.stduents_management.studenttuition.entity.StudentTuitionStatus;
import com.example.stduents_management.studenttuition.repository.StudentTuitionRepository;
import com.example.stduents_management.studenttuition.service.StudentTuitionService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PaymentRepository paymentRepository;
    private final StudentTuitionRepository studentTuitionRepository;
    private final StudentTuitionService studentTuitionService;
    private final NotificationService notificationService;

    public Page<PaymentResponse> search(String keyword, String statusStr, int page, int size) {
        PaymentStatus status = parseStatus(statusStr);
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.search(term, status, pageable).map(this::toResponse);
    }

    public PaymentResponse getById(Long id) {
        return paymentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy giao dịch thanh toán"));
    }

    public List<PaymentResponse> getAll() {
        return paymentRepository.findAllOrdered().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void create(PaymentRequest req) {
        StudentTuition tuition = resolveStudentTuition(req.getStudentTuitionId());
        ensureStudentTuitionTotalAmount(tuition);
        Payment entity = new Payment();
        buildEntity(entity, req, tuition);
        paymentRepository.save(entity);
        recalculateStudentTuition(tuition.getId());
        notifyTuitionPaymentChange(tuition.getId());
    }

    @Transactional
    public void update(Long id, PaymentRequest req) {
        Payment entity = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy giao dịch thanh toán"));
        UUID tuitionId = entity.getStudentTuition().getId();
        StudentTuition tuition = resolveStudentTuition(req.getStudentTuitionId());
        ensureStudentTuitionTotalAmount(tuition);
        buildEntity(entity, req, tuition);
        paymentRepository.save(entity);
        recalculateStudentTuition(tuitionId);
        notifyTuitionPaymentChange(tuitionId);
        if (!tuitionId.equals(tuition.getId())) {
            recalculateStudentTuition(tuition.getId());
            notifyTuitionPaymentChange(tuition.getId());
        }
    }

    @Transactional
    public void delete(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy giao dịch thanh toán"));
        UUID tuitionId = payment.getStudentTuition().getId();
        paymentRepository.delete(payment);
        recalculateStudentTuition(tuitionId);
        notifyTuitionPaymentChange(tuitionId);
    }

    private void notifyTuitionPaymentChange(UUID studentTuitionId) {
        if (studentTuitionId == null) return;
        StudentTuition updated = studentTuitionRepository.findById(studentTuitionId)
                .orElse(null);
        if (updated == null) return;
        if (updated.getStudent() == null || updated.getStudent().getUser() == null) return;
        if (updated.getTotalAmount() == null || updated.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal total = updated.getTotalAmount();
        BigDecimal paid = updated.getAmountPaid() != null ? updated.getAmountPaid() : BigDecimal.ZERO;

        int percent = total.compareTo(BigDecimal.ZERO) > 0
                ? paid.multiply(BigDecimal.valueOf(100))
                .divide(total, 0, java.math.RoundingMode.HALF_UP)
                .intValue()
                : 0;
        if (percent > 100) percent = 100;

        BigDecimal remaining = updated.getRemainingAmount() != null ? updated.getRemainingAmount() : BigDecimal.ZERO;
        String semesterCode = updated.getSemester() != null ? updated.getSemester().getCode() : "";
        String semesterName = updated.getSemester() != null ? updated.getSemester().getName() : "";
        String semesterLabel = !semesterCode.isBlank()
                ? semesterCode + (semesterName.isBlank() ? "" : " - " + semesterName)
                : semesterName;

        StudentTuitionStatus st = updated.getStatus();
        String statusLabel = st != null ? st.getLabel() : "";

        String title = NotificationCategory.TUITION_FEE.getLabel();
        String content = "Học phí học kỳ " + (semesterLabel.isBlank() ? "" : "(" + semesterLabel + ") ") +
                "đã được cập nhật. " +
                "Tiến độ: " + percent + "%. " +
                "Đã đóng: " + paid.toPlainString() + " / " + total.toPlainString() + " VNĐ. " +
                (remaining.compareTo(BigDecimal.ZERO) > 0
                        ? "Còn lại: " + remaining.toPlainString() + " VNĐ."
                        : "Bạn đã đóng đủ học phí. ") +
                (statusLabel.isBlank() ? "" : " (" + statusLabel + ")");

        notificationService.upsertForUserBySource(
                updated.getStudent().getUser().getId(),
                NotificationCategory.TUITION_FEE,
                title,
                content,
                null,
                NotificationCategory.TUITION_FEE.name(),
                studentTuitionId.toString()
        );
    }

    /**
     * Cập nhật lại amount_paid và status của học phí học kỳ dựa trên tổng các giao dịch COMPLETED.
     */
    private void recalculateStudentTuition(UUID studentTuitionId) {
        StudentTuition tuition = studentTuitionRepository.findById(studentTuitionId).orElse(null);
        if (tuition == null) return;

        ensureStudentTuitionTotalAmount(tuition);

        BigDecimal sumPaid = paymentRepository.findByStudentTuition_IdOrderByPaymentDateDesc(studentTuitionId)
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        tuition.setAmountPaid(sumPaid);
        BigDecimal remaining = tuition.getTotalAmount().subtract(sumPaid);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }
        tuition.setRemainingAmount(remaining);
        if (tuition.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            tuition.setStatus(StudentTuitionStatus.PAID);
        } else if (sumPaid.compareTo(BigDecimal.ZERO) > 0) {
            tuition.setStatus(StudentTuitionStatus.PARTIAL);
        } else {
            tuition.setStatus(StudentTuitionStatus.UNPAID);
        }
        studentTuitionRepository.save(tuition);
    }

    /**
     * Chuẩn hoá liên kết nghiệp vụ với cấu hình học phí (tuition_fees):
     * Nếu học phí học kỳ chưa có totalAmount (hoặc =0), tự tính lại theo:
     * totalAmount = totalCredits × fee_per_credit(ACTIVE) của ngành tương ứng.
     */
    private void ensureStudentTuitionTotalAmount(StudentTuition tuition) {
        if (tuition == null) return;
        if (tuition.getTotalAmount() != null && tuition.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) return;
        if (tuition.getTotalCredits() == null || tuition.getTotalCredits() <= 0) return;
        BigDecimal autoAmount = studentTuitionService.calculateTuitionAmountForStudent(
                tuition.getStudent(),
                tuition.getSemester(),
                tuition.getTotalCredits()
        );
        if (autoAmount != null && autoAmount.compareTo(BigDecimal.ZERO) > 0) {
            tuition.setTotalAmount(autoAmount);
            studentTuitionRepository.save(tuition);
        }
    }

    public void exportExcel(HttpServletResponse response) {
        List<Payment> all = paymentRepository.findAllOrdered();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Lịch sử thanh toán");
            setColumnWidths(sheet, 5000, 8000, 6000, 6000, 6000, 7000, 6000, 5000, 5000);

            CellStyle titleStyle = createTitleStyle(wb);
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LỊCH SỬ THANH TOÁN HỌC PHÍ");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            CellStyle headerStyle = createHeaderStyle(wb);
            Row header = sheet.createRow(1);
            String[] headers = {
                    "Mã SV", "Họ tên", "Học kỳ", "Năm học",
                    "Số tiền (VNĐ)", "Phương thức", "Mã GD", "Ngày thanh toán", "Trạng thái"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = createDataStyle(wb);
            int rowIdx = 2;
            for (Payment p : all) {
                Row row = sheet.createRow(rowIdx++);
                var st = p.getStudentTuition();
                row.createCell(0).setCellValue(st.getStudent().getStudentCode());
                row.createCell(1).setCellValue(st.getStudent().getFullName());
                row.createCell(2).setCellValue(st.getSemester().getCode());
                row.createCell(3).setCellValue(st.getSemester().getAcademicYear());
                row.createCell(4).setCellValue(p.getAmount().toPlainString());
                row.createCell(5).setCellValue(p.getPaymentMethod().getLabel());
                row.createCell(6).setCellValue(p.getTransactionCode() != null ? p.getTransactionCode() : "");
                row.createCell(7).setCellValue(p.getPaymentDate().format(DATE_TIME_FMT));
                row.createCell(8).setCellValue(p.getStatus().getLabel());
                for (int c = 0; c < 9; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=payments.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể xuất file Excel: " + e.getMessage());
        }
    }

    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }
        int count = 0;
        Set<UUID> tuitionIdsModified = new HashSet<>();
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String studentCode = getCellString(row.getCell(0));
                    String semesterCode = getCellString(row.getCell(2));
                    String academicYear = getCellString(row.getCell(3));
                    String amountStr = getCellString(row.getCell(4));
                    String methodStr = getCellString(row.getCell(5));
                    String transCode = getCellString(row.getCell(6));
                    String dateStr = getCellString(row.getCell(7));
                    String statusStr = getCellString(row.getCell(8));

                    if (studentCode.isBlank() || semesterCode.isBlank() || academicYear.isBlank() || amountStr.isBlank())
                        continue;

                    var tuition = findStudentTuitionByStudentCodeAndSemester(studentCode, semesterCode, academicYear);
                    if (tuition == null) continue;

                    BigDecimal amount = parseMoney(amountStr);
                    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) continue;

                    PaymentMethod method = parsePaymentMethod(methodStr);
                    LocalDateTime paymentDate = parseDateTime(dateStr);
                    PaymentStatus status = parseStatus(statusStr) != null ? parseStatus(statusStr) : PaymentStatus.COMPLETED;

                    Payment p = new Payment();
                    p.setStudentTuition(tuition);
                    p.setAmount(amount);
                    p.setPaymentMethod(method);
                    p.setTransactionCode(transCode.isBlank() ? null : transCode);
                    p.setPaymentDate(paymentDate != null ? paymentDate : LocalDateTime.now());
                    p.setStatus(status);
                    paymentRepository.save(p);
                    tuitionIdsModified.add(tuition.getId());
                    count++;
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ: " + e.getMessage());
        }
        tuitionIdsModified.forEach(this::recalculateStudentTuition);
        return count;
    }

    private StudentTuition findStudentTuitionByStudentCodeAndSemester(String studentCode, String semesterCode, String academicYear) {
        return studentTuitionRepository.findAllOrdered().stream()
                .filter(st -> st.getStudent().getStudentCode().equalsIgnoreCase(studentCode)
                        && st.getSemester().getCode().equalsIgnoreCase(semesterCode)
                        && st.getSemester().getAcademicYear().equalsIgnoreCase(academicYear))
                .findFirst()
                .orElse(null);
    }

    private void buildEntity(Payment entity, PaymentRequest req, StudentTuition tuition) {
        entity.setStudentTuition(tuition);
        entity.setAmount(req.getAmount());
        entity.setPaymentMethod(req.getPaymentMethod());
        entity.setTransactionCode(req.getTransactionCode() != null && !req.getTransactionCode().isBlank()
                ? req.getTransactionCode().trim() : null);
        entity.setPaymentDate(req.getPaymentDate() != null ? req.getPaymentDate() : LocalDateTime.now());
        entity.setStatus(req.getStatus());
    }

    private PaymentResponse toResponse(Payment p) {
        var st = p.getStudentTuition();
        var s = st.getStudent();
        var sem = st.getSemester();
        return new PaymentResponse(
                p.getId(),
                st.getId(),
                s.getStudentCode(),
                s.getFullName(),
                sem.getId(),
                sem.getCode(),
                sem.getName(),
                p.getAmount(),
                p.getPaymentMethod(),
                p.getPaymentMethod().getLabel(),
                p.getTransactionCode(),
                p.getPaymentDate(),
                p.getStatus(),
                p.getStatus().getLabel(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private StudentTuition resolveStudentTuition(UUID id) {
        return studentTuitionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy bản ghi học phí học kỳ"));
    }

    private PaymentStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return PaymentStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private PaymentMethod parsePaymentMethod(String s) {
        if (s == null || s.isBlank()) return PaymentMethod.CASH;
        String u = s.toUpperCase().replace(" ", "_");
        for (var m : PaymentMethod.values()) {
            if (m.name().equals(u)) return m;
        }
        if (s.contains("chuyển") || s.contains("bank")) return PaymentMethod.BANK_TRANSFER;
        if (s.contains("ví") || s.contains("wallet")) return PaymentMethod.E_WALLET;
        if (s.contains("trực tuyến") || s.contains("online")) return PaymentMethod.ONLINE_PAYMENT;
        return PaymentMethod.CASH;
    }

    private static void setColumnWidths(Sheet sheet, int... widths) {
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i]);
        }
    }

    private static CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private static BigDecimal parseMoney(String s) {
        if (s == null) return null;
        String cleaned = s.replaceAll("[^0-9.]", "");
        if (cleaned.isBlank()) return null;
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            if (s.contains("/")) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                if (s.length() <= 10) fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDateTime.parse(s.length() > 10 ? s : s + " 00:00", fmt);
            }
            return LocalDateTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}

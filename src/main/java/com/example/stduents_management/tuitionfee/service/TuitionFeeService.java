package com.example.stduents_management.tuitionfee.service;

import com.example.stduents_management.trainingprogram.entity.TrainingProgram;
import com.example.stduents_management.trainingprogram.repository.TrainingProgramRepository;
import com.example.stduents_management.tuitionfee.dto.TuitionFeeRequest;
import com.example.stduents_management.tuitionfee.dto.TuitionFeeResponse;
import com.example.stduents_management.tuitionfee.entity.TuitionFee;
import com.example.stduents_management.tuitionfee.entity.TuitionFeeStatus;
import com.example.stduents_management.tuitionfee.repository.TuitionFeeRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TuitionFeeService {

    private final TuitionFeeRepository tuitionFeeRepository;
    private final TrainingProgramRepository trainingProgramRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ─── SEARCH / LIST ────────────────────────────────────────────────────────

    public Page<TuitionFeeResponse> search(String keyword, String statusStr, int page, int size) {
        TuitionFeeStatus status = parseStatus(statusStr);
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size);
        return tuitionFeeRepository.search(term, status, pageable).map(this::toResponse);
    }

    public TuitionFeeResponse getById(UUID id) {
        return tuitionFeeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy mức học phí"));
    }

    public List<TuitionFeeResponse> getAll() {
        return tuitionFeeRepository.findAllOrdered().stream().map(this::toResponse).toList();
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @Transactional
    public void create(TuitionFeeRequest req) {
        TrainingProgram program = resolveProgram(req.getProgramId());
        handleActiveConflict(program.getProgramId(), null, req);

        TuitionFee entity = new TuitionFee();
        buildEntity(entity, req, program);
        tuitionFeeRepository.save(entity);
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    @Transactional
    public void update(UUID id, TuitionFeeRequest req) {
        TuitionFee entity = tuitionFeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy mức học phí"));

        TrainingProgram program = resolveProgram(req.getProgramId());
        handleActiveConflict(program.getProgramId(), id, req);

        buildEntity(entity, req, program);
        tuitionFeeRepository.save(entity);
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @Transactional
    public void delete(UUID id) {
        if (!tuitionFeeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy mức học phí");
        }
        tuitionFeeRepository.deleteById(id);
    }

    // ─── TOGGLE STATUS ────────────────────────────────────────────────────────

    @Transactional
    public void toggleStatus(UUID id) {
        TuitionFee entity = tuitionFeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy mức học phí"));

        if (entity.getStatus() == TuitionFeeStatus.INACTIVE) {
            // Khi kích hoạt lại, kiểm tra xem đã có ACTIVE nào chưa
            boolean conflict = tuitionFeeRepository.existsActiveForProgram(
                    entity.getTrainingProgram().getProgramId(), id);
            if (conflict) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Chương trình đào tạo này đã có mức học phí đang áp dụng. " +
                        "Vui lòng ngừng áp dụng mức cũ trước.");
            }
            entity.setStatus(TuitionFeeStatus.ACTIVE);
        } else {
            entity.setStatus(TuitionFeeStatus.INACTIVE);
        }
        tuitionFeeRepository.save(entity);
    }

    // ─── EXPORT EXCEL ─────────────────────────────────────────────────────────

    public void exportExcel(HttpServletResponse response) {
        List<TuitionFee> all = tuitionFeeRepository.findAllOrdered();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Học phí");
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 10000);
            sheet.setColumnWidth(2, 6000);
            sheet.setColumnWidth(3, 5000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 12000);

            // Title row
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BẢNG CẤU HÌNH HỌC PHÍ");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Header row
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

            Row header = sheet.createRow(1);
            String[] headers = {
                "Mã CTĐT", "Tên chương trình đào tạo", "Ngành",
                "Học phí/tín chỉ (VNĐ)", "Ngày áp dụng", "Trạng thái", "Ghi chú"
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
            for (TuitionFee tf : all) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(tf.getTrainingProgram().getProgramCode());
                row.createCell(1).setCellValue(tf.getTrainingProgram().getProgramName());
                row.createCell(2).setCellValue(
                        tf.getTrainingProgram().getMajor() != null
                        ? tf.getTrainingProgram().getMajor().getMajorName() : "");
                row.createCell(3).setCellValue(tf.getFeePerCredit().toPlainString());
                row.createCell(4).setCellValue(tf.getEffectiveDate().format(DATE_FMT));
                row.createCell(5).setCellValue(tf.getStatus().getLabel());
                row.createCell(6).setCellValue(tf.getNote() != null ? tf.getNote() : "");
                for (int c = 0; c < 7; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=tuition_fees.xlsx");
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
                    String programCode  = getCellString(row.getCell(0));
                    String feeStr       = getCellString(row.getCell(3));
                    String dateStr      = getCellString(row.getCell(4));
                    String statusStr    = getCellString(row.getCell(5));
                    String note         = getCellString(row.getCell(6));

                    if (programCode.isBlank() || feeStr.isBlank() || dateStr.isBlank()) continue;

                    TrainingProgram program = trainingProgramRepository.findAll()
                            .stream()
                            .filter(p -> p.getProgramCode().equalsIgnoreCase(programCode))
                            .findFirst()
                            .orElse(null);
                    if (program == null) continue;

                    BigDecimal fee;
                    try {
                        fee = new BigDecimal(feeStr.replaceAll("[^0-9.]", ""));
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    LocalDate effectiveDate;
                    try {
                        // Hỗ trợ cả dd/MM/yyyy và yyyy-MM-dd
                        if (dateStr.contains("/")) {
                            effectiveDate = LocalDate.parse(dateStr, DATE_FMT);
                        } else {
                            effectiveDate = LocalDate.parse(dateStr);
                        }
                    } catch (Exception e) {
                        continue;
                    }

                    TuitionFeeStatus status = TuitionFeeStatus.ACTIVE;
                    if (statusStr.equalsIgnoreCase("Ngừng áp dụng")
                            || statusStr.equalsIgnoreCase("INACTIVE")) {
                        status = TuitionFeeStatus.INACTIVE;
                    }

                    TuitionFee tf = new TuitionFee();
                    tf.setTrainingProgram(program);
                    tf.setFeePerCredit(fee);
                    tf.setEffectiveDate(effectiveDate);
                    tf.setStatus(status);
                    tf.setNote(note.isBlank() ? null : note);
                    tuitionFeeRepository.save(tf);
                    count++;

                } catch (Exception e) {
                    // Bỏ qua dòng lỗi, tiếp tục dòng tiếp theo
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ: " + e.getMessage());
        }
        return count;
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private TrainingProgram resolveProgram(UUID programId) {
        return trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy chương trình đào tạo"));
    }

    /**
     * Xử lý xung đột ACTIVE: nếu đã có bản ghi ACTIVE khác cho cùng chương trình,
     * ném lỗi (hoặc tự động deactivate nếu cờ autoDeactivatePrevious được bật).
     */
    private void handleActiveConflict(UUID programId, UUID excludeId, TuitionFeeRequest req) {
        if (req.getStatus() != TuitionFeeStatus.ACTIVE) return;

        boolean conflict = tuitionFeeRepository.existsActiveForProgram(programId, excludeId);
        if (!conflict) return;

        if (req.isAutoDeactivatePrevious()) {
            tuitionFeeRepository.findActiveByProgram(programId).ifPresent(prev -> {
                if (!prev.getId().equals(excludeId)) {
                    prev.setStatus(TuitionFeeStatus.INACTIVE);
                    tuitionFeeRepository.save(prev);
                }
            });
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Chương trình đào tạo này đã có mức học phí đang áp dụng. " +
                    "Bật tùy chọn \"Tự động ngừng áp dụng mức cũ\" để tiếp tục.");
        }
    }

    private void buildEntity(TuitionFee entity, TuitionFeeRequest req, TrainingProgram program) {
        entity.setTrainingProgram(program);
        entity.setFeePerCredit(req.getFeePerCredit());
        entity.setEffectiveDate(req.getEffectiveDate());
        entity.setStatus(req.getStatus());
        entity.setNote(req.getNote() != null && !req.getNote().isBlank()
                ? req.getNote().trim() : null);
    }

    public TuitionFeeResponse toResponse(TuitionFee tf) {
        TrainingProgram p = tf.getTrainingProgram();
        return new TuitionFeeResponse(
                tf.getId(),
                p.getProgramId(),
                p.getProgramCode(),
                p.getProgramName(),
                p.getMajor() != null ? p.getMajor().getMajorName() : "",
                tf.getFeePerCredit(),
                tf.getEffectiveDate(),
                tf.getStatus(),
                tf.getStatus().getLabel(),
                tf.getNote(),
                tf.getCreatedAt(),
                tf.getUpdatedAt()
        );
    }

    private TuitionFeeStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return TuitionFeeStatus.valueOf(s.toUpperCase());
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
}

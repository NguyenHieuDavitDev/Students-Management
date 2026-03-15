package com.example.stduents_management.examtype.service;

import com.example.stduents_management.examtype.dto.ExamTypeRequest;
import com.example.stduents_management.examtype.dto.ExamTypeResponse;
import com.example.stduents_management.examtype.entity.ExamType;
import com.example.stduents_management.examtype.repository.ExamTypeRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamTypeService {

    private final ExamTypeRepository examTypeRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Page<ExamTypeResponse> search(
            String keyword,
            String fromDateStr,
            String toDateStr,
            int page,
            int size
    ) {
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        LocalDateTime from = parseDateStart(fromDateStr);
        LocalDateTime to = parseDateEnd(toDateStr);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return examTypeRepository.search(term, from, to, pageable).map(this::toResponse);
    }

    /** Lấy toàn bộ kết quả theo bộ lọc (dùng cho trang in). */
    public List<ExamTypeResponse> getAllFiltered(String keyword, String fromDateStr, String toDateStr) {
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        LocalDateTime from = parseDateStart(fromDateStr);
        LocalDateTime to = parseDateEnd(toDateStr);
        return examTypeRepository.search(term, from, to, Pageable.unpaged())
                .stream().map(this::toResponse).toList();
    }

    public ExamTypeResponse getById(UUID id) {
        return examTypeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy loại kỳ thi"));
    }

    public List<ExamTypeResponse> getAll() {
        return examTypeRepository.findAllOrdered().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void create(ExamTypeRequest req) {
        ExamType e = new ExamType();
        buildEntity(e, req);
        examTypeRepository.save(e);
    }

    @Transactional
    public void update(UUID id, ExamTypeRequest req) {
        ExamType e = examTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy loại kỳ thi"));
        buildEntity(e, req);
        examTypeRepository.save(e);
    }

    @Transactional
    public void delete(UUID id) {
        if (!examTypeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy loại kỳ thi");
        }
        examTypeRepository.deleteById(id);
    }

    public void exportExcel(HttpServletResponse response) {
        List<ExamType> all = examTypeRepository.findAllOrdered();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Loại kỳ thi");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 12000);
            sheet.setColumnWidth(2, 18000);
            sheet.setColumnWidth(3, 6000);

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH MỤC LOẠI KỲ THI HỌC PHẦN");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

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
            String[] headers = {"ID", "Tên loại kỳ thi", "Mô tả", "Ngày tạo"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowIdx = 2;
            for (ExamType e : all) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getId() != null ? e.getId().toString() : "");
                row.createCell(1).setCellValue(e.getName());
                row.createCell(2).setCellValue(e.getDescription() != null ? e.getDescription() : "");
                row.createCell(3).setCellValue(
                        e.getCreatedAt() != null
                                ? e.getCreatedAt().format(DATETIME_FMT)
                                : ""
                );
                for (int c = 0; c < 4; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=exam_types.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();

        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể xuất file Excel: " + ex.getMessage());
        }
    }

    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }

        int count = 0;
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getCellString(row.getCell(0));
                    String desc = getCellString(row.getCell(1));

                    if (name.isBlank()) continue;

                    ExamType e = new ExamType();
                    e.setName(name.trim());
                    e.setDescription(desc != null && !desc.isBlank() ? desc.trim() : null);
                    examTypeRepository.save(e);
                    count++;
                } catch (Exception ignore) {
                    // bỏ qua dòng lỗi
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ: " + e.getMessage());
        }
        return count;
    }

    private ExamTypeResponse toResponse(ExamType e) {
        return new ExamTypeResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private void buildEntity(ExamType e, ExamTypeRequest req) {
        e.setName(req.getName().trim());
        e.setDescription(
                req.getDescription() != null && !req.getDescription().isBlank()
                        ? req.getDescription().trim()
                        : null
        );
    }

    private LocalDateTime parseDateStart(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(s);
            return d.atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateEnd(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(s);
            return d.atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }

    private String getCellString(Cell cell) {
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
}

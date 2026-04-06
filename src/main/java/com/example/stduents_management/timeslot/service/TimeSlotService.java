package com.example.stduents_management.timeslot.service;

import com.example.stduents_management.timeslot.TimeSlotDayPart;
import com.example.stduents_management.timeslot.dto.TimeSlotRequest;
import com.example.stduents_management.timeslot.dto.TimeSlotResponse;
import com.example.stduents_management.timeslot.entity.TimeSlot;
import com.example.stduents_management.timeslot.repository.TimeSlotRepository;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final TimeSlotRepository repository;

    public Page<TimeSlotResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("slotCode"));
        Page<TimeSlot> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository.findBySlotCodeContainingIgnoreCase(keyword.trim(), pageable);
        return data.map(this::toResponse);
    }

    public TimeSlotResponse getById(Integer id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ"));
    }

    @Transactional
    public void create(TimeSlotRequest req) {
        if (repository.existsBySlotCode(req.getSlotCode().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã khung giờ đã tồn tại");
        }
        validatePeriodAndTime(req);
        repository.save(build(new TimeSlot(), req));
    }

    @Transactional
    public void update(Integer id, TimeSlotRequest req) {
        TimeSlot t = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ"));
        if (repository.existsBySlotCodeAndIdNot(req.getSlotCode().trim(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã khung giờ đã tồn tại");
        }
        validatePeriodAndTime(req);
        build(t, req);
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ");
        }
        repository.deleteById(id);
    }

    public List<TimeSlotResponse> getForPrint() {
        return repository.findAll(Sort.by("slotCode"))
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
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String slotCode = readString(row, 0, formatter);
                if (slotCode == null || slotCode.isBlank()) continue;
                slotCode = slotCode.trim();
                if (repository.existsBySlotCode(slotCode)) continue;

                Integer periodStart = readInt(row, 1, formatter);
                Integer periodEnd = readInt(row, 2, formatter);
                LocalTime startTime = readTime(row, 3, formatter);
                LocalTime endTime = readTime(row, 4, formatter);
                Boolean isActive = readBoolean(row, 5, formatter);

                if (periodStart == null) periodStart = 1;
                if (periodEnd == null) periodEnd = 1;
                if (startTime == null) startTime = LocalTime.of(7, 0);
                if (endTime == null) endTime = LocalTime.of(9, 30);
                if (isActive == null) isActive = true;

                TimeSlot slot = new TimeSlot();
                slot.setSlotCode(slotCode);
                slot.setPeriodStart(periodStart);
                slot.setPeriodEnd(periodEnd);
                slot.setStartTime(startTime);
                slot.setEndTime(endTime);
                slot.setIsActive(isActive);
                repository.save(slot);
            }
        }
    }

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("TimeSlots");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Slot Code");
            header.createCell(1).setCellValue("Period Start");
            header.createCell(2).setCellValue("Period End");
            header.createCell(3).setCellValue("Start Time");
            header.createCell(4).setCellValue("End Time");
            header.createCell(5).setCellValue("Is Active");

            List<TimeSlot> list = repository.findAll(Sort.by("slotCode"));
            int rowNum = 1;
            for (TimeSlot t : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nullToEmpty(t.getSlotCode()));
                row.createCell(1).setCellValue(t.getPeriodStart() != null ? t.getPeriodStart() : 0);
                row.createCell(2).setCellValue(t.getPeriodEnd() != null ? t.getPeriodEnd() : 0);
                row.createCell(3).setCellValue(t.getStartTime() != null ? t.getStartTime().format(TIME_FORMAT) : "");
                row.createCell(4).setCellValue(t.getEndTime() != null ? t.getEndTime().format(TIME_FORMAT) : "");
                row.createCell(5).setCellValue(Boolean.TRUE.equals(t.getIsActive()) ? "1" : "0");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void validatePeriodAndTime(TimeSlotRequest req) {
        if (req.getPeriodStart() != null && req.getPeriodEnd() != null && req.getPeriodEnd() < req.getPeriodStart()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tiết kết thúc phải >= tiết bắt đầu");
        }
        if (req.getStartTime() != null && req.getEndTime() != null && !req.getEndTime().isAfter(req.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giờ kết thúc phải sau giờ bắt đầu");
        }
    }

    private TimeSlot build(TimeSlot t, TimeSlotRequest req) {
        t.setSlotCode(req.getSlotCode().trim());
        t.setPeriodStart(req.getPeriodStart());
        t.setPeriodEnd(req.getPeriodEnd());
        t.setStartTime(req.getStartTime());
        t.setEndTime(req.getEndTime());
        t.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        return t;
    }

    private TimeSlotResponse toResponse(TimeSlot t) {
        TimeSlotDayPart part = TimeSlotDayPart.resolve(t);
        return new TimeSlotResponse(
                t.getId(),
                t.getSlotCode(),
                t.getPeriodStart(),
                t.getPeriodEnd(),
                t.getStartTime(),
                t.getEndTime(),
                t.getIsActive(),
                part.getApiValue(),
                part.getLabelVi()
        );
    }

    private static String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String v = formatter.formatCellValue(cell);
        return v != null ? v.trim() : null;
    }

    private static Integer readInt(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        String v = formatter.formatCellValue(cell);
        if (v == null || v.isBlank()) return null;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalTime readTime(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            try {
                return cell.getLocalDateTimeCellValue().toLocalTime();
            } catch (Exception e) {
                // fallback to string
            }
        }
        String v = formatter.formatCellValue(cell);
        if (v == null || v.isBlank()) return null;
        v = v.trim();
        if (v.length() <= 5 && v.contains(":")) {
            try {
                return LocalTime.parse(v, TIME_FORMAT);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static Boolean readBoolean(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue() != 0;
        }
        String v = formatter.formatCellValue(cell);
        if (v == null || v.isBlank()) return null;
        return "1".equals(v.trim()) || "true".equalsIgnoreCase(v.trim()) || "x".equalsIgnoreCase(v.trim());
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}

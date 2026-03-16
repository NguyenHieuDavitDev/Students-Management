package com.example.stduents_management.examroom.service;

import com.example.stduents_management.examroom.dto.ExamRoomRequest;
import com.example.stduents_management.examroom.dto.ExamRoomResponse;
import com.example.stduents_management.examroom.entity.ExamRoom;
import com.example.stduents_management.examroom.repository.ExamRoomRepository;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.room.repository.RoomRepository;
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
public class ExamRoomService {

    private final ExamRoomRepository examRoomRepository;
    private final RoomRepository roomRepository;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Page<ExamRoomResponse> search(String keyword, String fromDateStr, String toDateStr, int page, int size) {
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        LocalDateTime from = parseDateStart(fromDateStr);
        LocalDateTime to = parseDateEnd(toDateStr);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "room.roomCode"));
        return examRoomRepository.search(term, from, to, pageable).map(this::toResponse);
    }

    public List<ExamRoomResponse> getAllFiltered(String keyword, String fromDateStr, String toDateStr) {
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        LocalDateTime from = parseDateStart(fromDateStr);
        LocalDateTime to = parseDateEnd(toDateStr);
        return examRoomRepository.search(term, from, to, Pageable.unpaged())
            .stream().map(this::toResponse).toList();
    }

    public ExamRoomResponse getById(UUID id) {
        return examRoomRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng thi"));
    }

    public List<ExamRoomResponse> getAll() {
        return examRoomRepository.findAllOrdered().stream().map(this::toResponse).toList();
    }

    public List<Room> getAllActiveRooms() {
        return roomRepository.findByIsActiveTrue();
    }

    @Transactional
    public void create(ExamRoomRequest req) {
        if (examRoomRepository.existsByRoom_RoomId(req.getRoomId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phòng này đã được phân làm phòng thi");
        }
        Room room = roomRepository.findById(req.getRoomId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng"));
        ExamRoom er = new ExamRoom();
        er.setRoom(room);
        er.setExamCapacity(req.getExamCapacity());
        er.setDescription(req.getDescription() != null && !req.getDescription().isBlank() ? req.getDescription().trim() : null);
        examRoomRepository.save(er);
    }

    @Transactional
    public void update(UUID id, ExamRoomRequest req) {
        ExamRoom er = examRoomRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng thi"));
        if (examRoomRepository.existsByRoom_RoomIdAndIdNot(req.getRoomId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phòng này đã được phân cho phòng thi khác");
        }
        Room room = roomRepository.findById(req.getRoomId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng"));
        er.setRoom(room);
        er.setExamCapacity(req.getExamCapacity());
        er.setDescription(req.getDescription() != null && !req.getDescription().isBlank() ? req.getDescription().trim() : null);
        examRoomRepository.save(er);
    }

    @Transactional
    public void delete(UUID id) {
        if (!examRoomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng thi");
        }
        examRoomRepository.deleteById(id);
    }

    public void exportExcel(HttpServletResponse response) {
        List<ExamRoom> all = examRoomRepository.findAllOrdered();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Phòng thi");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 12000);
            sheet.setColumnWidth(3, 12000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 5000);
            sheet.setColumnWidth(6, 15000);
            sheet.setColumnWidth(7, 6000);

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH PHÂN PHÒNG THI");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

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
            String[] headers = {"ID", "Mã phòng", "Tên phòng", "Toà nhà", "Sức chứa thi", "Sức chứa phòng", "Mô tả", "Ngày tạo"};
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
            for (ExamRoom er : all) {
                Row row = sheet.createRow(rowIdx++);
                Room r = er.getRoom();
                row.createCell(0).setCellValue(er.getId() != null ? er.getId().toString() : "");
                row.createCell(1).setCellValue(r.getRoomCode());
                row.createCell(2).setCellValue(r.getRoomName());
                row.createCell(3).setCellValue(r.getBuilding() != null ? r.getBuilding().getBuildingName() : "");
                row.createCell(4).setCellValue(er.getExamCapacity() != null ? er.getExamCapacity() : (r.getCapacity() != null ? r.getCapacity() : 0));
                row.createCell(5).setCellValue(r.getCapacity() != null ? r.getCapacity() : 0);
                row.createCell(6).setCellValue(er.getDescription() != null ? er.getDescription() : "");
                row.createCell(7).setCellValue(er.getCreatedAt() != null ? er.getCreatedAt().format(DATETIME_FMT) : "");
                for (int c = 0; c < 8; c++) row.getCell(c).setCellStyle(dataStyle);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=exam_rooms.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể xuất file Excel: " + e.getMessage());
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
                    String roomCode = getCellString(row.getCell(0));
                    String capStr = getCellString(row.getCell(1));
                    String desc = getCellString(row.getCell(2));
                    if (roomCode.isBlank()) continue;
                    Room room = roomRepository.findByRoomCodeIgnoreCase(roomCode.trim()).orElse(null);
                    if (room == null || examRoomRepository.existsByRoom_RoomId(room.getRoomId())) continue;
                    ExamRoom er = new ExamRoom();
                    er.setRoom(room);
                    if (capStr != null && !capStr.isBlank()) {
                        try {
                            er.setExamCapacity(Integer.parseInt(capStr.replaceAll("[^0-9]", "")));
                        } catch (NumberFormatException ignored) {}
                    }
                    er.setDescription(desc != null && !desc.isBlank() ? desc.trim() : null);
                    examRoomRepository.save(er);
                    count++;
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File Excel không hợp lệ: " + e.getMessage());
        }
        return count;
    }

    private ExamRoomResponse toResponse(ExamRoom er) {
        Room r = er.getRoom();
        return new ExamRoomResponse(
            er.getId(),
            r.getRoomId(),
            r.getRoomCode(),
            r.getRoomName(),
            r.getBuilding() != null ? r.getBuilding().getBuildingId() : null,
            r.getBuilding() != null ? r.getBuilding().getBuildingName() : "",
            er.getExamCapacity(),
            r.getCapacity(),
            er.getDescription(),
            er.getCreatedAt(),
            er.getUpdatedAt()
        );
    }

    private LocalDateTime parseDateStart(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateEnd(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s).atTime(23, 59, 59);
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

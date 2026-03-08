package com.example.stduents_management.roomblocktime.service;

import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.roomblocktime.dto.RoomBlockTimeRequest;
import com.example.stduents_management.roomblocktime.dto.RoomBlockTimeResponse;
import com.example.stduents_management.roomblocktime.entity.BlockStatus;
import com.example.stduents_management.roomblocktime.entity.BlockType;
import com.example.stduents_management.roomblocktime.entity.RoomBlockTime;
import com.example.stduents_management.roomblocktime.repository.RoomBlockTimeRepository;
import com.example.stduents_management.timeslot.entity.TimeSlot;
import com.example.stduents_management.timeslot.repository.TimeSlotRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomBlockTimeService {

    private final RoomBlockTimeRepository roomBlockTimeRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public Page<RoomBlockTimeResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchTerm = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<RoomBlockTime> data = roomBlockTimeRepository.searchByKeyword(searchTerm, pageable);
        return data.map(this::toResponse);
    }

    public RoomBlockTimeResponse getById(UUID id) {
        return roomBlockTimeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi khóa phòng"));
    }

    @Transactional
    public void create(RoomBlockTimeRequest req) {
        RoomBlockTime entity = new RoomBlockTime();
        buildEntity(entity, req);
        roomBlockTimeRepository.save(entity);
    }

    @Transactional
    public void update(UUID id, RoomBlockTimeRequest req) {
        RoomBlockTime entity = roomBlockTimeRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi khóa phòng"));
        buildEntity(entity, req);
    }

    @Transactional
    public void delete(UUID id) {
        if (!roomBlockTimeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản ghi khóa phòng");
        }
        roomBlockTimeRepository.deleteById(id);
    }

    public List<RoomBlockTimeResponse> getForPrint() {
        return roomBlockTimeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void buildEntity(RoomBlockTime entity, RoomBlockTimeRequest req) {
        Room room = roomRepository.findById(req.getRoomId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng học"));
        entity.setRoom(room);
        entity.setBlockType(req.getBlockType());
        entity.setDayOfWeek(req.getDayOfWeek());
        entity.setStartWeek(req.getStartWeek());
        entity.setEndWeek(req.getEndWeek());
        entity.setStartDate(req.getStartDate());
        entity.setEndDate(req.getEndDate());
        entity.setReason(req.getReason());
        entity.setStatus(req.getStatus() != null ? req.getStatus() : BlockStatus.ACTIVE);
        if (req.getTimeSlotId() != null) {
            TimeSlot slot = timeSlotRepository.findById(req.getTimeSlotId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khung giờ"));
            entity.setTimeSlot(slot);
        } else {
            entity.setTimeSlot(null);
        }
    }

    private RoomBlockTimeResponse toResponse(RoomBlockTime b) {
        Room r = b.getRoom();
        TimeSlot t = b.getTimeSlot();
        return new RoomBlockTimeResponse(
                b.getBlockId(),
                r != null ? r.getRoomId() : null,
                r != null ? r.getRoomCode() : null,
                r != null ? r.getRoomName() : null,
                b.getBlockType(),
                b.getDayOfWeek(),
                t != null ? t.getId() : null,
                t != null ? t.getSlotCode() : null,
                b.getStartWeek(),
                b.getEndWeek(),
                b.getStartDate(),
                b.getEndDate(),
                b.getReason(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }

    // ---------- Import Excel ----------

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        DataFormatter formatter = new DataFormatter(Locale.getDefault());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                int rowNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String roomCode = readString(row, 0, formatter);
                if (roomCode == null || roomCode.isBlank()) continue;

                Room room = roomRepository.findByRoomCodeIgnoreCase(roomCode)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy phòng với mã " + roomCode));

                BlockType blockType = readBlockType(row, 1, formatter, rowNum);
                Integer dayOfWeek = readIntInRange(row, 2, formatter, 2, 8);
                String slotCode = readString(row, 3, formatter);
                Integer startWeek = readInteger(row, 4, formatter);
                Integer endWeek = readInteger(row, 5, formatter);
                LocalDate startDate = readDate(row, 6, formatter);
                LocalDate endDate = readDate(row, 7, formatter);
                String reason = readString(row, 8, formatter);
                BlockStatus status = readBlockStatus(row, 9, formatter, rowNum);

                RoomBlockTime entity = new RoomBlockTime();
                entity.setRoom(room);
                entity.setBlockType(blockType);
                entity.setDayOfWeek(dayOfWeek);
                entity.setStartWeek(startWeek);
                entity.setEndWeek(endWeek);
                entity.setStartDate(startDate);
                entity.setEndDate(endDate);
                entity.setReason(reason);
                entity.setStatus(status != null ? status : BlockStatus.ACTIVE);
                if (slotCode != null && !slotCode.isBlank()) {
                    timeSlotRepository.findBySlotCodeIgnoreCase(slotCode).ifPresent(entity::setTimeSlot);
                }
                roomBlockTimeRepository.save(entity);
            }
        }
    }

    // ---------- Export Excel ----------

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Khóa phòng");
            Row header = sheet.createRow(0);
            String[] headers = {"Mã phòng", "Loại khóa", "Thứ", "Mã ca", "Tuần bắt đầu", "Tuần kết thúc", "Từ ngày", "Đến ngày", "Lý do", "Trạng thái"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            List<RoomBlockTime> list = roomBlockTimeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            int rowNum = 1;
            for (RoomBlockTime b : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(b.getRoom() != null ? b.getRoom().getRoomCode() : "");
                row.createCell(1).setCellValue(b.getBlockType() != null ? b.getBlockType().name() : "");
                row.createCell(2).setCellValue(b.getDayOfWeek() != null ? String.valueOf(b.getDayOfWeek()) : "");
                row.createCell(3).setCellValue(b.getTimeSlot() != null ? b.getTimeSlot().getSlotCode() : "");
                row.createCell(4).setCellValue(b.getStartWeek() != null ? String.valueOf(b.getStartWeek()) : "");
                row.createCell(5).setCellValue(b.getEndWeek() != null ? String.valueOf(b.getEndWeek()) : "");
                row.createCell(6).setCellValue(b.getStartDate() != null ? b.getStartDate().format(DATE_FORMAT) : "");
                row.createCell(7).setCellValue(b.getEndDate() != null ? b.getEndDate().format(DATE_FORMAT) : "");
                row.createCell(8).setCellValue(b.getReason() != null ? b.getReason() : "");
                row.createCell(9).setCellValue(b.getStatus() != null ? b.getStatus().name() : "");
            }
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

    private Integer readInteger(Row row, int cellIndex, DataFormatter formatter) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return null;
        try {
            return (int) Double.parseDouble(raw.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer readIntInRange(Row row, int cellIndex, DataFormatter formatter, int min, int max) {
        Integer v = readInteger(row, cellIndex, formatter);
        if (v == null) return null;
        if (v < min || v > max) return null;
        return v;
    }

    private LocalDate readDate(Row row, int cellIndex, DataFormatter formatter) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            try {
                return LocalDate.parse(raw.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private BlockType readBlockType(Row row, int cellIndex, DataFormatter formatter, int rowNum) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return BlockType.OTHER;
        try {
            return BlockType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dòng " + rowNum + ": Loại khóa không hợp lệ (MAINTENANCE, EVENT, EXAM, OTHER)");
        }
    }

    private BlockStatus readBlockStatus(Row row, int cellIndex, DataFormatter formatter, int rowNum) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return BlockStatus.ACTIVE;
        try {
            return BlockStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dòng " + rowNum + ": Trạng thái không hợp lệ (ACTIVE, CANCELLED)");
        }
    }
}

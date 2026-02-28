package com.example.stduents_management.equipment.service;

import com.example.stduents_management.equipment.dto.EquipmentRequest;
import com.example.stduents_management.equipment.dto.EquipmentResponse;
import com.example.stduents_management.equipment.entity.Equipment;
import com.example.stduents_management.equipment.entity.EquipmentStatus;
import com.example.stduents_management.equipment.repository.EquipmentRepository;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.room.repository.RoomRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RoomRepository roomRepository;

    public Page<EquipmentResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("equipmentCode"));
        Page<Equipment> data =
                (keyword == null || keyword.isBlank())
                        ? equipmentRepository.findAll(pageable)
                        : equipmentRepository.findByEquipmentCodeContainingIgnoreCaseOrEquipmentNameContainingIgnoreCase(
                        keyword, keyword, pageable);
        return data.map(this::toResponse);
    }

    public EquipmentResponse getById(Long id) {
        return equipmentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thiết bị"));
    }

    @Transactional
    public void create(EquipmentRequest req) {
        if (equipmentRepository.existsByEquipmentCode(req.getEquipmentCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã thiết bị đã tồn tại");
        }
        equipmentRepository.save(build(new Equipment(), req));
    }

    @Transactional
    public void update(Long id, EquipmentRequest req) {
        Equipment e = equipmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thiết bị"));
        if (equipmentRepository.existsByEquipmentCodeAndEquipmentIdNot(req.getEquipmentCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã thiết bị đã tồn tại");
        }
        build(e, req);
    }

    @Transactional
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thiết bị");
        }
        equipmentRepository.deleteById(id);
    }

    public List<EquipmentResponse> getForPrint() {
        return equipmentRepository.findAll(Sort.by("equipmentCode"))
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

                String equipmentCode = readString(row, 0, formatter);
                if (equipmentCode == null || equipmentCode.isBlank()) continue;
                if (equipmentRepository.existsByEquipmentCode(equipmentCode)) continue;

                String equipmentName = readString(row, 1, formatter);
                if (equipmentName == null || equipmentName.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Tên thiết bị không được để trống");
                }

                String serialNumber = readString(row, 2, formatter);
                LocalDate purchaseDate = readLocalDate(row, 3, formatter);

                EquipmentStatus status = EquipmentStatus.ACTIVE;
                String statusRaw = readString(row, 4, formatter);
                if (statusRaw != null && !statusRaw.isBlank()) {
                    try {
                        status = EquipmentStatus.valueOf(statusRaw.trim().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Dòng " + rowNum + ": Status không hợp lệ (ACTIVE, BROKEN, MAINTENANCE)");
                    }
                }

                String roomCodeRaw = readString(row, 5, formatter);
                Room room = null;
                if (roomCodeRaw != null && !roomCodeRaw.isBlank()) {
                    room = roomRepository.findByRoomCodeIgnoreCase(roomCodeRaw)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Dòng " + rowNum + ": Không tìm thấy phòng với mã " + roomCodeRaw));
                }

                Equipment e = new Equipment();
                e.setEquipmentCode(equipmentCode);
                e.setEquipmentName(equipmentName);
                e.setSerialNumber(serialNumber);
                e.setPurchaseDate(purchaseDate);
                e.setStatus(status);
                e.setRoom(room);
                equipmentRepository.save(e);
            }
        }
    }

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Equipments");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Equipment Code");
            header.createCell(1).setCellValue("Equipment Name");
            header.createCell(2).setCellValue("Serial Number");
            header.createCell(3).setCellValue("Purchase Date");
            header.createCell(4).setCellValue("Status");
            header.createCell(5).setCellValue("Room Code");
            header.createCell(6).setCellValue("Room Name");

            List<Equipment> list = equipmentRepository.findAll(Sort.by("equipmentCode"));
            int rowNum = 1;
            for (Equipment e : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nullToEmpty(e.getEquipmentCode()));
                row.createCell(1).setCellValue(nullToEmpty(e.getEquipmentName()));
                row.createCell(2).setCellValue(nullToEmpty(e.getSerialNumber()));
                row.createCell(3).setCellValue(e.getPurchaseDate() != null ? e.getPurchaseDate().toString() : "");
                row.createCell(4).setCellValue(e.getStatus() != null ? e.getStatus().name() : "");
                Room room = e.getRoom();
                row.createCell(5).setCellValue(room != null ? nullToEmpty(room.getRoomCode()) : "");
                row.createCell(6).setCellValue(room != null ? nullToEmpty(room.getRoomName()) : "");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private Equipment build(Equipment e, EquipmentRequest req) {
        e.setEquipmentCode(req.getEquipmentCode());
        e.setEquipmentName(req.getEquipmentName());
        e.setSerialNumber(req.getSerialNumber());
        e.setPurchaseDate(req.getPurchaseDate());
        e.setStatus(req.getStatus() != null ? req.getStatus() : EquipmentStatus.ACTIVE);
        Room room = null;
        if (req.getRoomId() != null) {
            room = roomRepository.findById(req.getRoomId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng học"));
        }
        e.setRoom(room);
        return e;
    }

    private EquipmentResponse toResponse(Equipment e) {
        Room room = e.getRoom();
        return new EquipmentResponse(
                e.getEquipmentId(),
                e.getEquipmentCode(),
                e.getEquipmentName(),
                e.getSerialNumber(),
                e.getPurchaseDate(),
                e.getStatus(),
                room != null ? room.getRoomId() : null,
                room != null ? room.getRoomCode() : null,
                room != null ? room.getRoomName() : null,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }

    private LocalDate readLocalDate(Row row, int cellIndex, DataFormatter formatter) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception ex) {
            return null;
        }
    }
}

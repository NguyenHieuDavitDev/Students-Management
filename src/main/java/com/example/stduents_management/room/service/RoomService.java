package com.example.stduents_management.room.service;

import com.example.stduents_management.building.entity.Building;
import com.example.stduents_management.building.repository.BuildingRepository;
import com.example.stduents_management.room.dto.RoomRequest;
import com.example.stduents_management.room.dto.RoomResponse;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.room.entity.RoomStatus;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.roomtype.entity.RoomType;
import com.example.stduents_management.roomtype.repository.RoomTypeRepository;
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
public class RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final RoomTypeRepository roomTypeRepository;

    public Page<RoomResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("roomCode"));

        Page<Room> data =
                (keyword == null || keyword.isBlank())
                        ? roomRepository.findAll(pageable)
                        : roomRepository
                        .findByRoomCodeContainingIgnoreCaseOrRoomNameContainingIgnoreCase(
                                keyword, keyword, pageable
                        );

        return data.map(this::toResponse);
    }

    public RoomResponse getById(Long id) {
        return roomRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng học"));
    }

    @Transactional
    public void create(RoomRequest req) {
        if (roomRepository.existsByRoomCode(req.getRoomCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã phòng học đã tồn tại");
        }
        roomRepository.save(build(new Room(), req));
    }

    @Transactional
    public void update(Long id, RoomRequest req) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng học"));

        if (roomRepository.existsByRoomCodeAndRoomIdNot(req.getRoomCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã phòng học đã tồn tại");
        }

        build(room, req);
    }

    @Transactional
    public void delete(Long id) {
        roomRepository.deleteById(id);
    }

    public List<RoomResponse> getForPrint() {
        return roomRepository.findAll(Sort.by("roomCode"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ================= IMPORT ================= */

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {

        DataFormatter formatter = new DataFormatter(Locale.getDefault());

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                final int rowNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String roomCode = readString(row, 0, formatter);
                if (roomCode == null || roomCode.isBlank()) continue;

                if (roomRepository.existsByRoomCode(roomCode)) {
                    continue;
                }

                String roomName = readString(row, 1, formatter);
                String buildingCode = readString(row, 2, formatter);
                String roomTypeCode = readString(row, 3, formatter);

                if (roomName == null || roomName.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": roomName không được để trống"
                    );
                }

                if (buildingCode == null || buildingCode.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": buildingCode không được để trống"
                    );
                }

                if (roomTypeCode == null || roomTypeCode.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": roomTypeCode không được để trống"
                    );
                }

                Building building = buildingRepository.findByBuildingCodeIgnoreCase(buildingCode)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Dòng " + rowNum + ": Không tìm thấy tòa nhà với mã " + buildingCode
                                ));

                RoomType roomType = roomTypeRepository.findByRoomTypeCodeIgnoreCase(roomTypeCode)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Dòng " + rowNum + ": Không tìm thấy loại phòng với mã " + roomTypeCode
                                ));

                Integer floor = readInteger(row, 4, formatter);
                Integer capacity = readInteger(row, 5, formatter);
                Double area = readDouble(row, 6, formatter);

                RoomStatus status = RoomStatus.AVAILABLE;
                String statusRaw = readString(row, 7, formatter);
                if (statusRaw != null && !statusRaw.isBlank()) {
                    try {
                        status = RoomStatus.valueOf(statusRaw.trim().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Dòng " + rowNum + ": status không hợp lệ (AVAILABLE, IN_USE, MAINTENANCE)"
                        );
                    }
                }

                Boolean isActive = Boolean.TRUE;
                String activeRaw = readString(row, 8, formatter);
                if (activeRaw != null && !activeRaw.isBlank()) {
                    isActive = activeRaw.trim().equalsIgnoreCase("true")
                            || activeRaw.trim().equalsIgnoreCase("1")
                            || activeRaw.trim().equalsIgnoreCase("yes");
                }

                Room r = new Room();
                r.setRoomCode(roomCode);
                r.setRoomName(roomName);
                r.setBuilding(building);
                r.setRoomType(roomType);
                r.setFloor(floor);
                r.setCapacity(capacity);
                r.setArea(area);
                r.setStatus(status);
                r.setIsActive(isActive);

                roomRepository.save(r);
            }
        }
    }

    /* ================= EXPORT ================= */

    public byte[] exportExcel() throws Exception {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Rooms");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Room Code");
            header.createCell(1).setCellValue("Room Name");
            header.createCell(2).setCellValue("Building Code");
            header.createCell(3).setCellValue("Room Type Code");
            header.createCell(4).setCellValue("Floor");
            header.createCell(5).setCellValue("Capacity");
            header.createCell(6).setCellValue("Area");
            header.createCell(7).setCellValue("Status");
            header.createCell(8).setCellValue("Active");

            List<Room> list = roomRepository.findAll(Sort.by("roomCode"));

            int rowNum = 1;
            for (Room r : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getRoomCode());
                row.createCell(1).setCellValue(r.getRoomName());
                row.createCell(2).setCellValue(r.getBuilding() != null ? r.getBuilding().getBuildingCode() : "");
                row.createCell(3).setCellValue(r.getRoomType() != null ? r.getRoomType().getRoomTypeCode() : "");
                row.createCell(4).setCellValue(r.getFloor() != null ? r.getFloor() : 0);
                row.createCell(5).setCellValue(r.getCapacity() != null ? r.getCapacity() : 0);
                row.createCell(6).setCellValue(r.getArea() != null ? r.getArea() : 0);
                row.createCell(7).setCellValue(r.getStatus() != null ? r.getStatus().name() : "");
                row.createCell(8).setCellValue(Boolean.TRUE.equals(r.getIsActive()));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /* =============== PRIVATE MAPPING =============== */

    private Room build(Room room, RoomRequest req) {
        UUID buildingId = req.getBuildingId();
        UUID roomTypeId = req.getRoomTypeId();

        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Tòa nhà không tồn tại"));

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Loại phòng không tồn tại"));

        room.setRoomCode(req.getRoomCode());
        room.setRoomName(req.getRoomName());
        room.setBuilding(building);
        room.setRoomType(roomType);
        room.setFloor(req.getFloor());
        room.setCapacity(req.getCapacity());
        room.setArea(req.getArea());
        room.setStatus(req.getStatus());
        room.setIsActive(req.getIsActive());

        return room;
    }

    private RoomResponse toResponse(Room room) {
        Building b = room.getBuilding();
        RoomType rt = room.getRoomType();

        return new RoomResponse(
                room.getRoomId(),
                room.getRoomCode(),
                room.getRoomName(),
                b != null ? b.getBuildingId() : null,
                b != null ? b.getBuildingCode() : null,
                b != null ? b.getBuildingName() : null,
                rt != null ? rt.getRoomTypeId() : null,
                rt != null ? rt.getRoomTypeCode() : null,
                rt != null ? rt.getRoomTypeName() : null,
                room.getFloor(),
                room.getCapacity(),
                room.getArea(),
                room.getStatus(),
                room.getIsActive(),
                room.getCreatedAt(),
                room.getUpdatedAt()
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
            Double d = Double.parseDouble(raw.replace(",", "."));
            return d.intValue();
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Double readDouble(Row row, int cellIndex, DataFormatter formatter) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return null;
        try {
            return Double.parseDouble(raw.replace(",", "."));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
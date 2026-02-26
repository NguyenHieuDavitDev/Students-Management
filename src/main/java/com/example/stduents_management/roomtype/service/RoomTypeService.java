package com.example.stduents_management.roomtype.service;

import com.example.stduents_management.roomtype.dto.*;
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
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final RoomTypeRepository repository;

    public Page<RoomTypeResponse> search(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("roomTypeCode"));

        Page<RoomType> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository
                        .findByRoomTypeCodeContainingIgnoreCaseOrRoomTypeNameContainingIgnoreCase(
                                keyword, keyword, pageable
                        );

        return data.map(this::toResponse);
    }

    public RoomTypeResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy loại phòng"));
    }

    @Transactional
    public void create(RoomTypeRequest req) {

        if (repository.existsByRoomTypeCode(req.getRoomTypeCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã loại phòng đã tồn tại");
        }

        repository.save(build(new RoomType(), req));
    }

    @Transactional
    public void update(UUID id, RoomTypeRequest req) {

        RoomType r = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy loại phòng"));

        if (repository.existsByRoomTypeCodeAndRoomTypeIdNot(req.getRoomTypeCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã loại phòng đã tồn tại");
        }

        build(r, req);
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public List<RoomTypeResponse> getForPrint() {
        return repository.findAll(Sort.by("roomTypeCode"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ================= IMPORT ================= */

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            RoomType r = new RoomType();
            r.setRoomTypeCode(row.getCell(0).getStringCellValue());
            r.setRoomTypeName(row.getCell(1).getStringCellValue());
            r.setDescription(row.getCell(2).getStringCellValue());
            r.setMaxCapacity((int) row.getCell(3).getNumericCellValue());

            repository.save(r);
        }
    }

    /* ================= EXPORT ================= */

    public byte[] exportExcel() throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("RoomTypes");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Code");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Description");
        header.createCell(3).setCellValue("Max Capacity");

        List<RoomType> list = repository.findAll();

        int rowNum = 1;
        for (RoomType r : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getRoomTypeCode());
            row.createCell(1).setCellValue(r.getRoomTypeName());
            row.createCell(2).setCellValue(r.getDescription());
            row.createCell(3).setCellValue(r.getMaxCapacity());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    /* ================= PRIVATE ================= */

    private RoomType build(RoomType r, RoomTypeRequest req) {
        r.setRoomTypeCode(req.getRoomTypeCode());
        r.setRoomTypeName(req.getRoomTypeName());
        r.setDescription(req.getDescription());
        r.setMaxCapacity(req.getMaxCapacity());
        return r;
    }

    private RoomTypeResponse toResponse(RoomType r) {
        return new RoomTypeResponse(
                r.getRoomTypeId(),
                r.getRoomTypeCode(),
                r.getRoomTypeName(),
                r.getDescription(),
                r.getMaxCapacity()
        );
    }
}
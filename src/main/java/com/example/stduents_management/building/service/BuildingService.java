package com.example.stduents_management.building.service;


import com.example.stduents_management.building.dto.BuildingRequest;
import com.example.stduents_management.building.dto.BuildingResponse;
import com.example.stduents_management.building.entity.Building;
import com.example.stduents_management.building.repository.BuildingRepository;
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
public class BuildingService {

    private final BuildingRepository repository;

    public Page<BuildingResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("buildingCode"));

        Page<Building> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository
                        .findByBuildingCodeContainingIgnoreCaseOrBuildingNameContainingIgnoreCase(
                                keyword, keyword, pageable
                        );

        return data.map(this::toResponse);
    }

    public BuildingResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tòa nhà"));
    }

    @Transactional
    public void create(BuildingRequest req) {
        if (repository.existsByBuildingCode(req.getBuildingCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã tòa nhà đã tồn tại");
        }
        repository.save(build(new Building(), req));
    }

    @Transactional
    public void update(UUID id, BuildingRequest req) {
        Building b = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tòa nhà"));

        if (repository.existsByBuildingCodeAndBuildingIdNot(req.getBuildingCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã tòa nhà đã tồn tại");
        }

        build(b, req);
    }

    @Transactional
    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public List<BuildingResponse> getForPrint() {
        return repository.findAll(Sort.by("buildingCode"))
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

            Building b = new Building();
            b.setBuildingCode(row.getCell(0).getStringCellValue());
            b.setBuildingName(row.getCell(1).getStringCellValue());
            b.setAddress(row.getCell(2).getStringCellValue());
            b.setNumberOfFloors((int) row.getCell(3).getNumericCellValue());
            b.setTotalArea(row.getCell(4).getNumericCellValue());
            repository.save(b);
        }
    }

    /* ================= EXPORT ================= */

    public byte[] exportExcel() throws Exception {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Buildings");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Code");
        header.createCell(1).setCellValue("Name");
        header.createCell(2).setCellValue("Address");
        header.createCell(3).setCellValue("Floors");
        header.createCell(4).setCellValue("Area");

        List<Building> list = repository.findAll();

        int rowNum = 1;
        for (Building b : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(b.getBuildingCode());
            row.createCell(1).setCellValue(b.getBuildingName());
            row.createCell(2).setCellValue(b.getAddress());
            row.createCell(3).setCellValue(b.getNumberOfFloors());
            row.createCell(4).setCellValue(b.getTotalArea());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    /* ================= PRIVATE ================= */

    private Building build(Building b, BuildingRequest req) {
        b.setBuildingCode(req.getBuildingCode());
        b.setBuildingName(req.getBuildingName());
        b.setAddress(req.getAddress());
        b.setNumberOfFloors(req.getNumberOfFloors());
        b.setTotalArea(req.getTotalArea());
        b.setDescription(req.getDescription());
        return b;
    }

    private BuildingResponse toResponse(Building b) {
        return new BuildingResponse(
                b.getBuildingId(),
                b.getBuildingCode(),
                b.getBuildingName(),
                b.getAddress(),
                b.getNumberOfFloors(),
                b.getTotalArea(),
                b.getDescription()
        );
    }
}
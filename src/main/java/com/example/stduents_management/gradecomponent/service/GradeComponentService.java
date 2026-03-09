package com.example.stduents_management.gradecomponent.service;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.gradecomponent.dto.GradeComponentRequest;
import com.example.stduents_management.gradecomponent.dto.GradeComponentResponse;
import com.example.stduents_management.gradecomponent.entity.GradeComponent;
import com.example.stduents_management.gradecomponent.repository.GradeComponentRepository;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GradeComponentService {

    private final GradeComponentRepository gradeComponentRepository;
    private final ClassSectionRepository classSectionRepository;

    public Page<GradeComponentResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String searchTerm = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<GradeComponent> data = gradeComponentRepository.searchByKeyword(searchTerm, pageable);
        return data.map(this::toResponse);
    }

    public GradeComponentResponse getById(UUID id) {
        return gradeComponentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thành phần điểm"));
    }

    @Transactional
    public void create(GradeComponentRequest req) {
        GradeComponent entity = new GradeComponent();
        buildEntity(entity, req);
        gradeComponentRepository.save(entity);
    }

    @Transactional
    public void update(UUID id, GradeComponentRequest req) {
        GradeComponent entity = gradeComponentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thành phần điểm"));
        buildEntity(entity, req);
    }

    @Transactional
    public void delete(UUID id) {
        if (!gradeComponentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thành phần điểm");
        }
        gradeComponentRepository.deleteById(id);
    }

    public List<GradeComponentResponse> getForPrint() {
        return gradeComponentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void buildEntity(GradeComponent entity, GradeComponentRequest req) {
        ClassSection cs = classSectionRepository.findById(req.getCourseClassId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
        entity.setClassSection(cs);
        entity.setComponentName(req.getComponentName());
        entity.setWeight(req.getWeight());
        entity.setMaxScore(req.getMaxScore());
    }

    private GradeComponentResponse toResponse(GradeComponent g) {
        ClassSection cs = g.getClassSection();
        return new GradeComponentResponse(
                g.getId(),
                cs != null ? cs.getId() : null,
                cs != null ? cs.getClassCode() : null,
                cs != null ? cs.getClassName() : null,
                cs != null && cs.getCourse() != null ? cs.getCourse().getCourseCode() : null,
                cs != null && cs.getCourse() != null ? cs.getCourse().getCourseName() : null,
                g.getComponentName(),
                g.getWeight(),
                g.getMaxScore(),
                g.getCreatedAt()
        );
    }

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        DataFormatter formatter = new DataFormatter(Locale.getDefault());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                int rowNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String classCode = readString(row, 0, formatter);
                if (classCode == null || classCode.isBlank()) continue;

                ClassSection cs = classSectionRepository.findByClassCodeIgnoreCase(classCode)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Dòng " + rowNum + ": Không tìm thấy lớp học phần với mã " + classCode));

                String componentName = readString(row, 1, formatter);
                if (componentName == null || componentName.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": Tên thành phần điểm không được để trống");
                }
                BigDecimal weight = readBigDecimal(row, 2, formatter);
                BigDecimal maxScore = readBigDecimal(row, 3, formatter);

                GradeComponent entity = new GradeComponent();
                entity.setClassSection(cs);
                entity.setComponentName(componentName.trim());
                entity.setWeight(weight);
                entity.setMaxScore(maxScore);
                gradeComponentRepository.save(entity);
            }
        }
    }

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Thành phần điểm");
            Row header = sheet.createRow(0);
            String[] headers = {"Mã lớp học phần", "Tên thành phần điểm", "Trọng số (%)", "Điểm tối đa"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            List<GradeComponent> list = gradeComponentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            int rowNum = 1;
            for (GradeComponent g : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(g.getClassSection() != null ? g.getClassSection().getClassCode() : "");
                row.createCell(1).setCellValue(g.getComponentName() != null ? g.getComponentName() : "");
                row.createCell(2).setCellValue(g.getWeight() != null ? g.getWeight().doubleValue() : 0);
                row.createCell(3).setCellValue(g.getMaxScore() != null ? g.getMaxScore().doubleValue() : 0);
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

    private BigDecimal readBigDecimal(Row row, int cellIndex, DataFormatter formatter) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return null;
        try {
            return new BigDecimal(raw.replace(",", ".").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

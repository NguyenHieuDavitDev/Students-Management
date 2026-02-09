package com.example.stduents_management.educationtype.service;

import com.example.stduents_management.educationtype.dto.EducationTypeRequest;
import com.example.stduents_management.educationtype.dto.EducationTypeResponse;
import com.example.stduents_management.educationtype.entity.EducationType;
import com.example.stduents_management.educationtype.repository.EducationTypeRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EducationTypeService {

    private final EducationTypeRepository repository;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    /* ===== SEARCH + PAGINATION ===== */
    public Page<EducationTypeResponse> search(
            String keyword, int page, int size
    ) {
        Pageable pageable = PageRequest.of(
                page, size, Sort.by("educationTypeName")
        );

        Page<EducationType> result =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository.findByEducationTypeNameContainingIgnoreCase(
                        keyword, pageable
                );

        return result.map(e ->
                new EducationTypeResponse(
                        e.getEducationTypeId(),
                        e.getEducationTypeName(),
                        e.getIsActive(),
                        e.getCreatedAt(),
                        e.getUpdatedAt()
                )
        );
    }

    public EducationTypeResponse getById(UUID id) {
        EducationType e = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy hệ đào tạo"
                ));

        return new EducationTypeResponse(
                e.getEducationTypeId(),
                e.getEducationTypeName(),
                e.getIsActive(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    /* ===== CREATE ===== */
    @Transactional
    public EducationTypeResponse create(EducationTypeRequest req) {
        String name = normalize(req.getEducationTypeName());

        if (repository.existsByEducationTypeNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Hệ đào tạo đã tồn tại"
            );
        }

        EducationType e = new EducationType();
        e.setEducationTypeName(name);
        e.setIsActive(req.getIsActive());

        repository.save(e);

        return getById(e.getEducationTypeId());
    }

    /* ===== UPDATE ===== */
    @Transactional
    public EducationTypeResponse update(
            UUID id, EducationTypeRequest req
    ) {
        EducationType e = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy hệ đào tạo"
                ));

        String name = normalize(req.getEducationTypeName());

        if (repository.existsByEducationTypeNameIgnoreCaseAndEducationTypeIdNot(
                name, id
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên hệ đào tạo đã tồn tại"
            );
        }

        e.setEducationTypeName(name);
        e.setIsActive(req.getIsActive());

        repository.save(e);
        return getById(id);
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy hệ đào tạo"
            );
        }
        repository.deleteById(id);
    }

    /* ===== EXPORT ===== */
    public void exportExcel(HttpServletResponse response) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("EducationTypes");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tên hệ đào tạo");
            header.createCell(1).setCellValue("Trạng thái");

            List<EducationType> list =
                    repository.findAll(Sort.by("educationTypeName"));

            int rowIdx = 1;
            for (EducationType e : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getEducationTypeName());
                row.createCell(1).setCellValue(
                        e.getIsActive() ? "Hoạt động" : "Ngưng"
                );
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=education-types.xlsx"
            );

            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể export Excel"
            );
        }
    }

    /* ===== IMPORT ===== */
    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File rỗng"
            );
        }

        int count = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = row.getCell(0)
                        .getStringCellValue().trim();

                if (repository.existsByEducationTypeNameIgnoreCase(name))
                    continue;

                EducationType e = new EducationType();
                e.setEducationTypeName(name);
                e.setIsActive(true);

                repository.save(e);
                count++;
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ"
            );
        }

        return count;
    }

    public List<EducationTypeResponse> getForPrint() {
        return repository.findAll(Sort.by("educationTypeName"))
                .stream()
                .map(e -> new EducationTypeResponse(
                        e.getEducationTypeId(),
                        e.getEducationTypeName(),
                        e.getIsActive(),
                        e.getCreatedAt(),
                        e.getUpdatedAt()
                ))
                .toList();
    }
}

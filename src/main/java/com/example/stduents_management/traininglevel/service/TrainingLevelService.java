package com.example.stduents_management.traininglevel.service;

import com.example.stduents_management.traininglevel.dto.TrainingLevelRequest;
import com.example.stduents_management.traininglevel.dto.TrainingLevelResponse;
import com.example.stduents_management.traininglevel.entity.TrainingLevel;
import com.example.stduents_management.traininglevel.repository.TrainingLevelRepository;
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
public class TrainingLevelService {

    private final TrainingLevelRepository trainingLevelRepository;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    /* ================= SEARCH ================= */
    public Page<TrainingLevelResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("trainingLevelName"));

        Page<TrainingLevel> levels =
                (keyword == null || keyword.isBlank())
                        ? trainingLevelRepository.findAll(pageable)
                        : trainingLevelRepository
                        .findByTrainingLevelNameContainingIgnoreCase(keyword, pageable);

        return levels.map(l ->
                new TrainingLevelResponse(
                        l.getTrainingLevelId(),
                        l.getTrainingLevelName(),
                        l.getDescription()
                )
        );
    }

    public TrainingLevelResponse getById(UUID id) {
        TrainingLevel l = trainingLevelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy trình độ đào tạo"
                ));

        return new TrainingLevelResponse(
                l.getTrainingLevelId(),
                l.getTrainingLevelName(),
                l.getDescription()
        );
    }

    /* ================= CREATE ================= */
    @Transactional
    public TrainingLevelResponse create(TrainingLevelRequest req) {
        String name = normalize(req.getTrainingLevelName());

        if (trainingLevelRepository.existsByTrainingLevelNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Trình độ đào tạo đã tồn tại"
            );
        }

        TrainingLevel l = new TrainingLevel();
        l.setTrainingLevelName(name);
        l.setDescription(req.getDescription());
        trainingLevelRepository.save(l);

        return new TrainingLevelResponse(
                l.getTrainingLevelId(),
                l.getTrainingLevelName(),
                l.getDescription()
        );
    }

    /* ================= UPDATE ================= */
    @Transactional
    public TrainingLevelResponse update(UUID id, TrainingLevelRequest req) {
        TrainingLevel l = trainingLevelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy trình độ đào tạo"
                ));

        String name = normalize(req.getTrainingLevelName());

        if (trainingLevelRepository
                .existsByTrainingLevelNameIgnoreCaseAndTrainingLevelIdNot(name, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Trình độ đào tạo đã tồn tại"
            );
        }

        l.setTrainingLevelName(name);
        l.setDescription(req.getDescription());
        trainingLevelRepository.save(l);

        return new TrainingLevelResponse(
                l.getTrainingLevelId(),
                l.getTrainingLevelName(),
                l.getDescription()
        );
    }

    @Transactional
    public void delete(UUID id) {
        if (!trainingLevelRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy trình độ đào tạo"
            );
        }
        trainingLevelRepository.deleteById(id);
    }

    /* ================= EXPORT EXCEL ================= */
    public void exportExcel(HttpServletResponse response) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("TrainingLevels");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tên trình độ");
            header.createCell(1).setCellValue("Mô tả");

            List<TrainingLevel> list =
                    trainingLevelRepository.findAll(Sort.by("trainingLevelName"));

            int rowIdx = 1;
            for (TrainingLevel l : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(l.getTrainingLevelName());
                row.createCell(1)
                        .setCellValue(l.getDescription() == null ? "" : l.getDescription());
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=training_levels.xlsx"
            );

            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể export Excel"
            );
        }
    }

    /* ================= IMPORT EXCEL ================= */
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

                Cell cell = row.getCell(0);
                if (cell == null) continue;

                String name;
                if (cell.getCellType() == CellType.STRING) {
                    name = cell.getStringCellValue().trim();
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    name = String.valueOf((long) cell.getNumericCellValue()).trim();
                } else {
                    continue;
                }
                if (name.isBlank()) continue;

                if (trainingLevelRepository.existsByTrainingLevelNameIgnoreCase(name))
                    continue;

                TrainingLevel l = new TrainingLevel();
                l.setTrainingLevelName(name);

                Cell descCell = row.getCell(1);
                if (descCell != null) {
                    String desc;
                    if (descCell.getCellType() == CellType.STRING) {
                        desc = descCell.getStringCellValue();
                    } else if (descCell.getCellType() == CellType.NUMERIC) {
                        desc = String.valueOf((long) descCell.getNumericCellValue());
                    } else {
                        desc = "";
                    }
                    l.setDescription(desc);
                }

                trainingLevelRepository.save(l);
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

    /* ================= PRINT ================= */
    public List<TrainingLevelResponse> getForPrint() {
        return trainingLevelRepository
                .findAll(Sort.by("trainingLevelName"))
                .stream()
                .map(l ->
                        new TrainingLevelResponse(
                                l.getTrainingLevelId(),
                                l.getTrainingLevelName(),
                                l.getDescription()
                        )
                )
                .toList();
    }
}

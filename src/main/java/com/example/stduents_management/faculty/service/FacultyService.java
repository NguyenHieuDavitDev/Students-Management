package com.example.stduents_management.faculty.service;

import com.example.stduents_management.faculty.dto.FacultyRequest;
import com.example.stduents_management.faculty.dto.FacultyResponse;
import com.example.stduents_management.faculty.entity.Faculty;
import com.example.stduents_management.faculty.repository.FacultyRepository;
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
public class FacultyService {

    private final FacultyRepository facultyRepository;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }


    public Page<FacultyResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("facultyName"));

        Page<Faculty> result =
                (keyword == null || keyword.isBlank())
                        ? facultyRepository.findAll(pageable)
                        : facultyRepository
                        .findByFacultyCodeContainingIgnoreCaseOrFacultyNameContainingIgnoreCase(
                                keyword, keyword, pageable
                        );

        return result.map(f ->
                new FacultyResponse(
                        f.getFacultyId(),
                        f.getFacultyCode(),
                        f.getFacultyName()
                )
        );
    }

    public FacultyResponse getById(UUID id) {
        Faculty f = facultyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Faculty not found"
                ));

        return new FacultyResponse(
                f.getFacultyId(),
                f.getFacultyCode(),
                f.getFacultyName()
        );
    }

    @Transactional
    public FacultyResponse create(FacultyRequest req) {
        String code = normalize(req.getFacultyCode());
        String name = normalize(req.getFacultyName());

        if (facultyRepository.existsByFacultyCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Faculty code already exists"
            );
        }

        Faculty f = new Faculty();
        f.setFacultyCode(code);
        f.setFacultyName(name);
        facultyRepository.save(f);

        return new FacultyResponse(
                f.getFacultyId(), f.getFacultyCode(), f.getFacultyName()
        );
    }

    @Transactional
    public FacultyResponse update(UUID id, FacultyRequest req) {
        Faculty f = facultyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Faculty not found"
                ));

        String code = normalize(req.getFacultyCode());
        String name = normalize(req.getFacultyName());

        if (facultyRepository.existsByFacultyCodeAndFacultyIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Faculty code already exists"
            );
        }

        f.setFacultyCode(code);
        f.setFacultyName(name);
        facultyRepository.save(f);

        return new FacultyResponse(
                f.getFacultyId(), f.getFacultyCode(), f.getFacultyName()
        );
    }

    @Transactional
    public void delete(UUID id) {
        if (!facultyRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Faculty not found"
            );
        }
        facultyRepository.deleteById(id);
    }


    public void exportExcel(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Faculties");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Faculty Code");
            header.createCell(1).setCellValue("Faculty Name");

            List<Faculty> faculties =
                    facultyRepository.findAll(Sort.by("facultyName"));

            int rowIdx = 1;
            for (Faculty f : faculties) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(f.getFacultyCode());
                row.createCell(1).setCellValue(f.getFacultyName());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=faculties.xlsx"
            );

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Cannot export Excel"
            );
        }
    }


    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File is empty"
            );
        }

        int count = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String code = getCell(row, 0);
                String name = getCell(row, 1);

                if (code.isBlank() || name.isBlank()) continue;
                if (facultyRepository.existsByFacultyCode(code)) continue;

                Faculty f = new Faculty();
                f.setFacultyCode(code);
                f.setFacultyName(name);
                facultyRepository.save(f);
                count++;
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Excel file"
            );
        }
        return count;
    }

    private String getCell(Row row, int index) {
        if (row.getCell(index) == null) return "";
        row.getCell(index).setCellType(CellType.STRING);
        return row.getCell(index).getStringCellValue().trim();
    }


    public List<FacultyResponse> getForPrint() {
        return facultyRepository.findAll(Sort.by("facultyName"))
                .stream()
                .map(f -> new FacultyResponse(
                        f.getFacultyId(),
                        f.getFacultyCode(),
                        f.getFacultyName()
                ))
                .toList();
    }
}

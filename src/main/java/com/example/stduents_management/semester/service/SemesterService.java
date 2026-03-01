package com.example.stduents_management.semester.service;

import com.example.stduents_management.semester.dto.SemesterRequest;
import com.example.stduents_management.semester.dto.SemesterResponse;
import com.example.stduents_management.semester.entity.Semester;
import com.example.stduents_management.semester.entity.SemesterStatus;
import com.example.stduents_management.semester.repository.SemesterRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SemesterService {

    private final SemesterRepository repository;

    public Page<SemesterResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));

        Page<Semester> data =
                (keyword == null || keyword.isBlank())
                        ? repository.findAll(pageable)
                        : repository.searchByCodeOrName(keyword, pageable);

        return data.map(this::toResponse);
    }

    public SemesterResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ"));
    }

    @Transactional
    public void create(SemesterRequest req) {
        validateBusinessRules(req, null);

        if (repository.existsByCode(req.getCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã học kỳ đã tồn tại");
        }

        repository.save(build(new Semester(), req));
    }

    @Transactional
    public void update(Long id, SemesterRequest req) {
        Semester s = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ"));

        validateBusinessRules(req, id);

        if (repository.existsByCodeAndIdNot(req.getCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã học kỳ đã tồn tại");
        }

        build(s, req);
    }

    @Transactional
    public void delete(Long id) {
        Semester s = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học kỳ"));

        if (s.getStatus() == SemesterStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa học kỳ đang mở (status = OPEN)"
            );
        }

        repository.deleteById(id);
    }

    public SemesterResponse getCurrentOpenSemester() {
        List<Semester> list = repository.findByStatus(SemesterStatus.OPEN);
        return list.stream()
                .sorted(Comparator.comparing(Semester::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không có học kỳ đang mở"));
    }

    public List<SemesterResponse> getForPrint() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "startDate"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ================= IMPORT / EXPORT ================= */

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {

        DataFormatter formatter = new DataFormatter(Locale.getDefault());

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                final int rowNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String code = readString(row, 0, formatter);
                if (code == null || code.isBlank()) continue;

                if (repository.existsByCode(code.trim())) {
                    continue;
                }

                String name = readString(row, 1, formatter);
                String academicYear = readString(row, 2, formatter);
                Integer term = readInteger(row, 3, formatter);
                LocalDate startDate = readLocalDate(row, 4, formatter);
                LocalDate endDate = readLocalDate(row, 5, formatter);
                LocalDate regStart = readLocalDate(row, 6, formatter);
                LocalDate regEnd = readLocalDate(row, 7, formatter);
                String statusRaw = readString(row, 8, formatter);
                String description = readString(row, 9, formatter);

                SemesterStatus status = SemesterStatus.UPCOMING;
                if (statusRaw != null && !statusRaw.isBlank()) {
                    try {
                        status = SemesterStatus.valueOf(statusRaw.trim().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Dòng " + rowNum + ": status không hợp lệ (UPCOMING, OPEN, CLOSED)"
                        );
                    }
                }

                SemesterRequest req = new SemesterRequest();
                req.setCode(code);
                req.setName(name != null ? name : "");
                req.setAcademicYear(academicYear != null ? academicYear : "");
                req.setTerm(term);
                req.setStartDate(startDate);
                req.setEndDate(endDate);
                req.setRegistrationStart(regStart);
                req.setRegistrationEnd(regEnd);
                req.setStatus(status);
                req.setDescription(description);

                validateBusinessRules(req, null);

                if (repository.existsByCode(req.getCode())) {
                    continue;
                }

                repository.save(build(new Semester(), req));
            }
        }
    }

    public byte[] exportExcel() throws Exception {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Semesters");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Code");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Academic Year");
            header.createCell(3).setCellValue("Term");
            header.createCell(4).setCellValue("Start Date");
            header.createCell(5).setCellValue("End Date");
            header.createCell(6).setCellValue("Registration Start");
            header.createCell(7).setCellValue("Registration End");
            header.createCell(8).setCellValue("Status");
            header.createCell(9).setCellValue("Description");

            List<Semester> list = repository.findAll(Sort.by(Sort.Direction.DESC, "startDate"));

            int rowNum = 1;
            for (Semester s : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nullToEmpty(s.getCode()));
                row.createCell(1).setCellValue(nullToEmpty(s.getName()));
                row.createCell(2).setCellValue(nullToEmpty(s.getAcademicYear()));
                row.createCell(3).setCellValue(s.getTerm() != null ? s.getTerm() : 0);
                row.createCell(4).setCellValue(s.getStartDate() != null ? s.getStartDate().toString() : "");
                row.createCell(5).setCellValue(s.getEndDate() != null ? s.getEndDate().toString() : "");
                row.createCell(6).setCellValue(s.getRegistrationStart() != null ? s.getRegistrationStart().toString() : "");
                row.createCell(7).setCellValue(s.getRegistrationEnd() != null ? s.getRegistrationEnd().toString() : "");
                row.createCell(8).setCellValue(s.getStatus() != null ? s.getStatus().name() : "");
                row.createCell(9).setCellValue(nullToEmpty(s.getDescription()));
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /* ================= PRIVATE ================= */

    private void validateBusinessRules(SemesterRequest req, Long id) {
        LocalDate start = req.getStartDate();
        LocalDate end = req.getEndDate();
        LocalDate regStart = req.getRegistrationStart();
        LocalDate regEnd = req.getRegistrationEnd();

        if (start != null && end != null && !start.isBefore(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate phải nhỏ hơn endDate"
            );
        }

        if (regStart != null && regEnd != null && regStart.isAfter(regEnd)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "registrationStart phải nhỏ hơn hoặc bằng registrationEnd"
            );
        }

        if (regEnd != null && start != null && regEnd.isAfter(start)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "registrationEnd phải nhỏ hơn hoặc bằng startDate"
            );
        }

        Integer term = req.getTerm();
        if (term != null && (term < 1 || term > 3)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "term chỉ được phép là 1, 2 hoặc 3"
            );
        }
    }

    private Semester build(Semester s, SemesterRequest req) {
        s.setCode(req.getCode() != null ? req.getCode().trim() : "");
        s.setName(req.getName() != null ? req.getName().trim() : "");
        s.setAcademicYear(req.getAcademicYear() != null ? req.getAcademicYear().trim() : "");
        s.setTerm(req.getTerm());
        s.setStartDate(req.getStartDate());
        s.setEndDate(req.getEndDate());
        s.setRegistrationStart(req.getRegistrationStart());
        s.setRegistrationEnd(req.getRegistrationEnd());
        s.setStatus(req.getStatus() != null ? req.getStatus() : SemesterStatus.UPCOMING);
        s.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        return s;
    }

    private SemesterResponse toResponse(Semester s) {
        return new SemesterResponse(
                s.getId(),
                s.getCode(),
                s.getName(),
                s.getAcademicYear(),
                s.getTerm(),
                s.getStartDate(),
                s.getEndDate(),
                s.getRegistrationStart(),
                s.getRegistrationEnd(),
                s.getStatus(),
                s.getDescription(),
                s.getCreatedAt(),
                s.getUpdatedAt()
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
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giá trị số không hợp lệ ở cột " + (cellIndex + 1)
            );
        }
    }

    private LocalDate readLocalDate(Row row, int cellIndex, DataFormatter formatter) {
        String raw = readString(row, cellIndex, formatter);
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Giá trị ngày không hợp lệ ở cột " + (cellIndex + 1) + " (định dạng yyyy-MM-dd)"
            );
        }
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
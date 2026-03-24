package com.example.stduents_management.lecturerduty.service;

import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.lecturerduty.dto.LecturerDutyRequest;
import com.example.stduents_management.lecturerduty.dto.LecturerDutyResponse;
import com.example.stduents_management.lecturerduty.entity.LecturerDuty;
import com.example.stduents_management.lecturerduty.repository.LecturerDutyRepository;
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
public class LecturerDutyService {

    private final LecturerDutyRepository lecturerDutyRepository;
    private final LecturerRepository lecturerRepository;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public Page<LecturerDutyResponse> search(String keyword, int page, int size) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("dutyName"));

        Page<LecturerDuty> duties =
                (keyword == null || keyword.isBlank())
                        ? lecturerDutyRepository.findAll(pageable)
                        : lecturerDutyRepository
                        .findByDutyCodeContainingIgnoreCaseOrDutyNameContainingIgnoreCase(
                                keyword, keyword, pageable);

        return duties.map(d ->
                new LecturerDutyResponse(
                        d.getLecturerDutyId(),
                        d.getDutyCode(),
                        d.getDutyName(),
                        d.getDescription()
                )
        );
    }

    public LecturerDutyResponse getById(UUID id) {

        LecturerDuty d = lecturerDutyRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy chức vụ"));

        return new LecturerDutyResponse(
                d.getLecturerDutyId(),
                d.getDutyCode(),
                d.getDutyName(),
                d.getDescription()
        );
    }

    @Transactional
    public LecturerDutyResponse create(LecturerDutyRequest req) {

        String code = normalize(req.getDutyCode());
        String name = normalize(req.getDutyName());
        String description = normalize(req.getDescription());

        if (lecturerDutyRepository.existsByDutyCodeIgnoreCase(code))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã chức vụ đã tồn tại");

        if (lecturerDutyRepository.existsByDutyNameIgnoreCase(name))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên chức vụ đã tồn tại");

        LecturerDuty d = new LecturerDuty();
        d.setDutyCode(code);
        d.setDutyName(name);
        d.setDescription(description);

        lecturerDutyRepository.save(d);

        return new LecturerDutyResponse(
                d.getLecturerDutyId(),
                d.getDutyCode(),
                d.getDutyName(),
                d.getDescription()
        );
    }

    @Transactional
    public LecturerDutyResponse update(UUID id, LecturerDutyRequest req) {

        LecturerDuty d = lecturerDutyRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy chức vụ"));

        String code = normalize(req.getDutyCode());
        String name = normalize(req.getDutyName());
        String description = normalize(req.getDescription());

        if (lecturerDutyRepository
                .existsByDutyCodeIgnoreCaseAndLecturerDutyIdNot(code, id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã chức vụ đã tồn tại");

        if (lecturerDutyRepository
                .existsByDutyNameIgnoreCaseAndLecturerDutyIdNot(name, id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên chức vụ đã tồn tại");

        d.setDutyCode(code);
        d.setDutyName(name);
        d.setDescription(description);

        lecturerDutyRepository.save(d);

        return new LecturerDutyResponse(
                d.getLecturerDutyId(),
                d.getDutyCode(),
                d.getDutyName(),
                d.getDescription()
        );
    }

    @Transactional
    public void delete(UUID id) {
        if (!lecturerDutyRepository.existsById(id))
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy chức vụ");

        if (lecturerRepository.existsByLecturerDuty_LecturerDutyId(id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa chức vụ này vì đang có giảng viên sử dụng");

        lecturerDutyRepository.deleteById(id);
    }

    public void exportExcel(HttpServletResponse response) {

        try (Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("LecturerDuties");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã chức vụ");
            header.createCell(1).setCellValue("Tên chức vụ");
            header.createCell(2).setCellValue("Mô tả");

            List<LecturerDuty> list =
                    lecturerDutyRepository.findAll(
                            Sort.by("dutyName"));

            int rowIdx = 1;
            for (LecturerDuty d : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0)
                        .setCellValue(d.getDutyCode());
                row.createCell(1)
                        .setCellValue(d.getDutyName());
                row.createCell(2)
                        .setCellValue(d.getDescription() != null ? d.getDescription() : "");
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=lecturer-duties.xlsx");

            wb.write(response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể export Excel");
        }
    }

    @Transactional
    public int importExcel(MultipartFile file) {

        if (file == null || file.isEmpty())
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File rỗng");

        int count = 0;

        try (Workbook wb =
                     new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Cell codeCell = row.getCell(0);
                    Cell nameCell = row.getCell(1);

                    if (codeCell == null || nameCell == null) continue;

                    String code = getCellValueAsString(codeCell).trim();
                    String name = getCellValueAsString(nameCell).trim();

                    if (code.isEmpty() || name.isEmpty()) continue;

                    String description = "";
                    Cell descCell = row.getCell(2);
                    if (descCell != null) {
                        description = getCellValueAsString(descCell).trim();
                    }

                    if (lecturerDutyRepository
                            .existsByDutyCodeIgnoreCase(code))
                        continue;

                    LecturerDuty d = new LecturerDuty();
                    d.setDutyCode(code);
                    d.setDutyName(name);
                    d.setDescription(description.isEmpty() ? null : description);

                    lecturerDutyRepository.save(d);
                    count++;
                } catch (Exception e) {
                    continue;
                }
            }

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ");
        }

        return count;
    }

    public List<LecturerDutyResponse> getForPrint() {
        return lecturerDutyRepository.findAll(
                        Sort.by("dutyName"))
                .stream()
                .map(d ->
                        new LecturerDutyResponse(
                                d.getLecturerDutyId(),
                                d.getDutyCode(),
                                d.getDutyName(),
                                d.getDescription()))
                .toList();
    }
}

package com.example.stduents_management.position.service;

import com.example.stduents_management.position.dto.PositionRequest;
import com.example.stduents_management.position.dto.PositionResponse;
import com.example.stduents_management.position.entity.Position;
import com.example.stduents_management.position.repository.PositionRepository;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
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
public class PositionService {

    private final PositionRepository positionRepository;
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
                    // Xử lý số nguyên
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

    /* ================= SEARCH ================= */
    public Page<PositionResponse> search(String keyword, int page, int size) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("positionName"));

        Page<Position> positions =
                (keyword == null || keyword.isBlank())
                        ? positionRepository.findAll(pageable)
                        : positionRepository
                        .findByPositionCodeContainingIgnoreCaseOrPositionNameContainingIgnoreCase(
                                keyword, keyword, pageable
                        );

        return positions.map(p ->
                new PositionResponse(
                        p.getPositionId(),
                        p.getPositionCode(),
                        p.getPositionName(),
                        p.getDescription()
                )
        );
    }

    public PositionResponse getById(UUID id) {

        Position p = positionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy chức danh"));

        return new PositionResponse(
                p.getPositionId(),
                p.getPositionCode(),
                p.getPositionName(),
                p.getDescription()
        );
    }

    /* ================= CREATE ================= */
    @Transactional
    public PositionResponse create(PositionRequest req) {

        String code = normalize(req.getPositionCode());
        String name = normalize(req.getPositionName());
        String description = normalize(req.getDescription());

        if (positionRepository.existsByPositionCodeIgnoreCase(code))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã chức danh đã tồn tại");

        if (positionRepository.existsByPositionNameIgnoreCase(name))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên chức danh đã tồn tại");

        Position p = new Position();
        p.setPositionCode(code);
        p.setPositionName(name);
        p.setDescription(description);

        positionRepository.save(p);

        return new PositionResponse(
                p.getPositionId(),
                p.getPositionCode(),
                p.getPositionName(),
                p.getDescription()
        );
    }

    /* ================= UPDATE ================= */
    @Transactional
    public PositionResponse update(UUID id, PositionRequest req) {

        Position p = positionRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy chức danh"));

        String code = normalize(req.getPositionCode());
        String name = normalize(req.getPositionName());
        String description = normalize(req.getDescription());

        if (positionRepository
                .existsByPositionCodeIgnoreCaseAndPositionIdNot(code, id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã chức danh đã tồn tại");

        if (positionRepository
                .existsByPositionNameIgnoreCaseAndPositionIdNot(name, id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tên chức danh đã tồn tại");

        p.setPositionCode(code);
        p.setPositionName(name);
        p.setDescription(description);

        positionRepository.save(p);

        return new PositionResponse(
                p.getPositionId(),
                p.getPositionCode(),
                p.getPositionName(),
                p.getDescription()
        );
    }

    @Transactional
    public void delete(UUID id) {
        if (!positionRepository.existsById(id))
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy chức danh");

        // Kiểm tra xem có giảng viên nào đang sử dụng chức danh này không
        if (lecturerRepository.existsByPosition_PositionId(id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa chức danh này vì đang có giảng viên sử dụng");

        positionRepository.deleteById(id);
    }

    /* ================= EXPORT ================= */
    public void exportExcel(HttpServletResponse response) {

        try (Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("Positions");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã chức danh");
            header.createCell(1).setCellValue("Tên chức danh");
            header.createCell(2).setCellValue("Mô tả");

            List<Position> list =
                    positionRepository.findAll(
                            Sort.by("positionName"));

            int rowIdx = 1;
            for (Position p : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0)
                        .setCellValue(p.getPositionCode());
                row.createCell(1)
                        .setCellValue(p.getPositionName());
                row.createCell(2)
                        .setCellValue(p.getDescription() != null ? p.getDescription() : "");
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=positions.xlsx");

            wb.write(response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể export Excel");
        }
    }

    /* ================= IMPORT ================= */
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

                    if (positionRepository
                            .existsByPositionCodeIgnoreCase(code))
                        continue;

                    Position p = new Position();
                    p.setPositionCode(code);
                    p.setPositionName(name);
                    p.setDescription(description.isEmpty() ? null : description);

                    positionRepository.save(p);
                    count++;
                } catch (Exception e) {
                    // Bỏ qua dòng lỗi và tiếp tục
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

    public List<PositionResponse> getForPrint() {
        return positionRepository.findAll(
                        Sort.by("positionName"))
                .stream()
                .map(p ->
                        new PositionResponse(
                                p.getPositionId(),
                                p.getPositionCode(),
                                p.getPositionName(),
                                p.getDescription()))
                .toList();
    }
}

package com.example.stduents_management.department.service;

import com.example.stduents_management.department.dto.DepartmentRequest;
import com.example.stduents_management.department.dto.DepartmentResponse;
import com.example.stduents_management.department.entity.Department;
import com.example.stduents_management.department.repository.DepartmentRepository;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    public Page<DepartmentResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("departmentName"));
        Page<Department> departments =
                (keyword == null || keyword.isBlank())
                        ? departmentRepository.findAll(pageable)
                        : departmentRepository.findByDepartmentCodeContainingIgnoreCaseOrDepartmentNameContainingIgnoreCase(
                                keyword, keyword, pageable
                        );
        return departments.map(this::toResponse);
    }

    public DepartmentResponse getById(UUID id) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng ban"));
        return toResponse(d);
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest req) {
        String code = normalize(req.getDepartmentCode());
        String name = normalize(req.getDepartmentName());
        String description = normalize(req.getDescription());

        if (departmentRepository.existsByDepartmentCodeIgnoreCase(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã phòng ban đã tồn tại");
        }
        if (departmentRepository.existsByDepartmentNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên phòng ban đã tồn tại");
        }

        Department d = new Department();
        d.setDepartmentCode(code);
        d.setDepartmentName(name);
        d.setDescription(description);
        departmentRepository.save(d);
        return toResponse(d);
    }

    @Transactional
    public DepartmentResponse update(UUID id, DepartmentRequest req) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng ban"));

        String code = normalize(req.getDepartmentCode());
        String name = normalize(req.getDepartmentName());
        String description = normalize(req.getDescription());

        if (departmentRepository.existsByDepartmentCodeIgnoreCaseAndDepartmentIdNot(code, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã phòng ban đã tồn tại");
        }
        if (departmentRepository.existsByDepartmentNameIgnoreCaseAndDepartmentIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên phòng ban đã tồn tại");
        }

        d.setDepartmentCode(code);
        d.setDepartmentName(name);
        d.setDescription(description);
        departmentRepository.save(d);
        return toResponse(d);
    }

    @Transactional
    public void delete(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phòng ban");
        }
        if (lecturerRepository.existsByDepartment_DepartmentId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa phòng ban này vì đang có giảng viên sử dụng"
            );
        }
        departmentRepository.deleteById(id);
    }

    public List<DepartmentResponse> getForPrint() {
        return departmentRepository.findAll(Sort.by("departmentName"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void exportExcel(HttpServletResponse response) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Departments");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã phòng ban");
            header.createCell(1).setCellValue("Tên phòng ban");
            header.createCell(2).setCellValue("Mô tả");

            List<Department> list = departmentRepository.findAll(Sort.by("departmentName"));
            int rowIdx = 1;
            for (Department d : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(d.getDepartmentCode());
                row.createCell(1).setCellValue(d.getDepartmentName());
                row.createCell(2).setCellValue(d.getDescription() != null ? d.getDescription() : "");
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=departments.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể xuất Excel");
        }
    }

    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }

        int count = 0;
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String code = row.getCell(0) != null ? normalize(row.getCell(0).toString()) : "";
                String name = row.getCell(1) != null ? normalize(row.getCell(1).toString()) : "";
                String description = row.getCell(2) != null ? normalize(row.getCell(2).toString()) : null;
                if (code == null || code.isBlank() || name == null || name.isBlank()) {
                    continue;
                }
                if (departmentRepository.existsByDepartmentCodeIgnoreCase(code)) {
                    continue;
                }
                Department d = new Department();
                d.setDepartmentCode(code);
                d.setDepartmentName(name);
                d.setDescription(description == null || description.isBlank() ? null : description);
                departmentRepository.save(d);
                count++;
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File Excel không hợp lệ");
        }
        return count;
    }

    private DepartmentResponse toResponse(Department d) {
        return new DepartmentResponse(
                d.getDepartmentId(),
                d.getDepartmentCode(),
                d.getDepartmentName(),
                d.getDescription()
        );
    }
}

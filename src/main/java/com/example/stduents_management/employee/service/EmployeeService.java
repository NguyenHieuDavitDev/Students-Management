package com.example.stduents_management.employee.service;

import com.example.stduents_management.common.service.FileStorageService;
import com.example.stduents_management.department.entity.Department;
import com.example.stduents_management.department.repository.DepartmentRepository;
import com.example.stduents_management.employee.dto.EmployeeRequest;
import com.example.stduents_management.employee.dto.EmployeeResponse;
import com.example.stduents_management.employee.entity.Employee;
import com.example.stduents_management.employee.entity.EmployeeType;
import com.example.stduents_management.employee.repository.EmployeeRepository;
import com.example.stduents_management.position.entity.Position;
import com.example.stduents_management.position.repository.PositionRepository;
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
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final FileStorageService fileStorageService;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    public Page<EmployeeResponse> search(String keyword, EmployeeType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("employeeCode"));
        return employeeRepository.search(keyword, type, pageable).map(this::toResponse);
    }

    public EmployeeResponse getById(UUID id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân sự"));
        return toResponse(e);
    }

    @Transactional
    public EmployeeResponse create(EmployeeRequest req) {
        String code = normalize(req.getEmployeeCode());
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã nhân sự đã tồn tại");
        }
        Employee e = new Employee();
        build(e, req);
        employeeRepository.save(e);
        return toResponse(e);
    }

    @Transactional
    public EmployeeResponse update(UUID id, EmployeeRequest req) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân sự"));
        String code = normalize(req.getEmployeeCode());
        if (employeeRepository.existsByEmployeeCodeIgnoreCaseAndEmployeeIdNot(code, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã nhân sự đã tồn tại");
        }
        build(e, req);
        return toResponse(e);
    }

    @Transactional
    public void delete(UUID id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân sự");
        }
        employeeRepository.deleteById(id);
    }

    private void build(Employee e, EmployeeRequest req) {
        Position position = null;
        if (req.getPositionId() != null) {
            position = positionRepository.findById(req.getPositionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chức danh không tồn tại"));
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phòng ban không tồn tại"));
        }

        e.setEmployeeCode(normalize(req.getEmployeeCode()));
        e.setFullName(normalize(req.getFullName()));
        e.setDateOfBirth(req.getDateOfBirth());
        e.setGender(normalize(req.getGender()));
        e.setCitizenId(normalize(req.getCitizenId()));
        e.setEmail(normalize(req.getEmail()));
        e.setPhoneNumber(normalize(req.getPhoneNumber()));
        e.setAddress(normalize(req.getAddress()));
        e.setEmployeeType(req.getEmployeeType() != null ? req.getEmployeeType() : EmployeeType.OTHER);
        e.setStatus(normalize(req.getStatus()) != null ? normalize(req.getStatus()) : "ACTIVE");
        e.setPosition(position);
        e.setDepartment(department);

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            e.setAvatar(fileStorageService.store(req.getAvatarFile()));
        } else if (normalize(req.getAvatar()) != null) {
            e.setAvatar(normalize(req.getAvatar()));
        }
        // updatedAt handled by @UpdateTimestamp
    }

    public List<EmployeeResponse> getForPrint() {
        return employeeRepository.findAll(Sort.by("employeeCode"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void exportExcel(HttpServletResponse response) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Employees");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã nhân sự");
            header.createCell(1).setCellValue("Họ tên");
            header.createCell(2).setCellValue("Loại");
            header.createCell(3).setCellValue("Phòng ban");
            header.createCell(4).setCellValue("Chức danh");
            header.createCell(5).setCellValue("Trạng thái");

            List<Employee> list = employeeRepository.findAll(Sort.by("employeeCode"));
            int rowIdx = 1;
            for (Employee e : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getEmployeeCode());
                row.createCell(1).setCellValue(e.getFullName());
                row.createCell(2).setCellValue(e.getEmployeeType() != null ? e.getEmployeeType().name() : "");
                row.createCell(3).setCellValue(e.getDepartment() != null ? e.getDepartment().getDepartmentName() : "");
                row.createCell(4).setCellValue(e.getPosition() != null ? e.getPosition().getPositionName() : "");
                row.createCell(5).setCellValue(e.getStatus() != null ? e.getStatus() : "");
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=employees.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception ex) {
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
                if (row == null) continue;
                String code = row.getCell(0) != null ? normalize(row.getCell(0).toString()) : null;
                String name = row.getCell(1) != null ? normalize(row.getCell(1).toString()) : null;
                String type = row.getCell(2) != null ? normalize(row.getCell(2).toString()) : null;
                if (code == null || code.isBlank() || name == null || name.isBlank()) continue;
                if (employeeRepository.existsByEmployeeCodeIgnoreCase(code)) continue;

                Employee e = new Employee();
                e.setEmployeeCode(code);
                e.setFullName(name);
                try {
                    e.setEmployeeType(type != null ? EmployeeType.valueOf(type) : EmployeeType.OTHER);
                } catch (Exception ignore) {
                    e.setEmployeeType(EmployeeType.OTHER);
                }
                e.setStatus("ACTIVE");
                employeeRepository.save(e);
                count++;
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File Excel không hợp lệ");
        }
        return count;
    }

    private EmployeeResponse toResponse(Employee e) {
        return new EmployeeResponse(
                e.getEmployeeId(),
                e.getEmployeeCode(),
                e.getFullName(),
                e.getDateOfBirth(),
                e.getGender(),
                e.getCitizenId(),
                e.getEmail(),
                e.getPhoneNumber(),
                e.getAddress(),
                e.getAvatar(),
                e.getEmployeeType(),
                e.getStatus(),
                e.getPosition() != null ? e.getPosition().getPositionId() : null,
                e.getPosition() != null ? e.getPosition().getPositionName() : null,
                e.getDepartment() != null ? e.getDepartment().getDepartmentId() : null,
                e.getDepartment() != null ? e.getDepartment().getDepartmentName() : null,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}


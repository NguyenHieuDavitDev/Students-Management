package com.example.stduents_management.permission.service;

import com.example.stduents_management.permission.dto.RolePermissionRequest;
import com.example.stduents_management.permission.dto.RolePermissionResponse;
import com.example.stduents_management.permission.entity.Permission;
import com.example.stduents_management.permission.entity.RolePermission;
import com.example.stduents_management.permission.repository.PermissionRepository;
import com.example.stduents_management.permission.repository.RolePermissionRepository;
import com.example.stduents_management.role.entity.Role;
import com.example.stduents_management.role.repository.RoleRepository;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public Page<RolePermissionResponse> search(String keyword, int page, int size) {
        // Query findAllOrderByRoleNameAndPermissionCode đã có ORDER BY r.name, p.code;
        // không truyền Sort vào Pageable để tránh SQL Server báo lỗi "column specified more than once"
        Page<RolePermission> data = (keyword == null || keyword.isBlank())
                ? rolePermissionRepository.findAllOrderByRoleNameAndPermissionCode(PageRequest.of(page, size))
                : rolePermissionRepository.search(keyword.trim(), PageRequest.of(page, size, Sort.by("role.name").and(Sort.by("permission.code"))));
        return data.map(this::toResponse);
    }

    public RolePermissionResponse getById(UUID id) {
        return rolePermissionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phân quyền"));
    }

    @Transactional
    public void create(RolePermissionRequest req) {
        Role role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vai trò"));
        Permission permission = permissionRepository.findById(req.getPermissionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy quyền"));
        if (rolePermissionRepository.existsByRole_IdAndPermission_Id(role.getId(), permission.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vai trò đã được gán quyền này");
        }
        RolePermission rp = new RolePermission();
        rp.setRole(role);
        rp.setPermission(permission);
        rolePermissionRepository.save(rp);
    }

    @Transactional
    public void delete(UUID id) {
        if (!rolePermissionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phân quyền");
        }
        rolePermissionRepository.deleteById(id);
    }

    public List<RolePermissionResponse> getForPrint() {
        return rolePermissionRepository.findAllForPrint().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        DataFormatter formatter = new DataFormatter(Locale.getDefault());
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String roleName = readString(row, 0, formatter);
                String permissionCode = readString(row, 1, formatter);
                if (roleName == null || roleName.isBlank() || permissionCode == null || permissionCode.isBlank()) continue;

                final int rowNum = i + 1;
                Role role = roleRepository.findByNameIgnoreCase(roleName.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy vai trò " + roleName));
                Permission permission = permissionRepository.findByCodeIgnoreCase(permissionCode.trim())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dòng " + rowNum + ": Không tìm thấy quyền " + permissionCode));

                if (rolePermissionRepository.existsByRole_IdAndPermission_Id(role.getId(), permission.getId())) continue;

                RolePermission rp = new RolePermission();
                rp.setRole(role);
                rp.setPermission(permission);
                rolePermissionRepository.save(rp);
            }
        }
    }

    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("RolePermissions");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Vai trò (Role name)");
            header.createCell(1).setCellValue("Mã quyền (Permission code)");
            header.createCell(2).setCellValue("Tên quyền");

            List<RolePermission> list = rolePermissionRepository.findAllForPrint();
            int rowNum = 1;
            for (RolePermission rp : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rp.getRole().getName());
                row.createCell(1).setCellValue(rp.getPermission().getCode());
                row.createCell(2).setCellValue(rp.getPermission().getName());
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private RolePermissionResponse toResponse(RolePermission rp) {
        return new RolePermissionResponse(
                rp.getId(),
                rp.getRole().getId(),
                rp.getRole().getName(),
                rp.getPermission().getId(),
                rp.getPermission().getCode(),
                rp.getPermission().getName()
        );
    }

    private static String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String v = formatter.formatCellValue(cell);
        return v != null ? v.trim() : null;
    }
}

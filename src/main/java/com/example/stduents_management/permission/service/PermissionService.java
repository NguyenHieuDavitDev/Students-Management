package com.example.stduents_management.permission.service;

import com.example.stduents_management.permission.SidebarMenuDefinition;
import com.example.stduents_management.permission.dto.PermissionRequest;
import com.example.stduents_management.permission.dto.PermissionResponse;
import com.example.stduents_management.permission.entity.Permission;
import com.example.stduents_management.permission.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public Page<PermissionResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("code"));
        Page<Permission> data = (keyword == null || keyword.isBlank())
                ? permissionRepository.findAll(pageable)
                : permissionRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, keyword, pageable);
        return data.map(this::toResponse);
    }

    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll(Sort.by("code")).stream().map(this::toResponse).toList();
    }

    public PermissionResponse getById(UUID id) {
        return permissionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy quyền"));
    }

    @Transactional
    public void create(PermissionRequest req) {
        String code = req.getCode() != null ? req.getCode().trim() : null;
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã quyền không được để trống");
        }
        if (permissionRepository.existsByCodeIgnoreCase(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã quyền đã tồn tại");
        }
        Permission p = new Permission();
        p.setCode(code);
        p.setName(req.getName() != null ? req.getName().trim() : "");
        p.setDescription(req.getDescription() != null && req.getDescription().length() > 255 ? req.getDescription().substring(0, 255) : req.getDescription());
        p.setSidebarMenuKey(normalizeSidebarMenuKey(req.getSidebarMenuKey()));
        permissionRepository.save(p);
    }

    @Transactional
    public void update(UUID id, PermissionRequest req) {
        Permission p = permissionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy quyền"));
        String code = req.getCode() != null ? req.getCode().trim() : null;
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã quyền không được để trống");
        }
        if (permissionRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã quyền đã tồn tại");
        }
        p.setCode(code);
        p.setName(req.getName() != null ? req.getName().trim() : "");
        p.setDescription(req.getDescription() != null && req.getDescription().length() > 255 ? req.getDescription().substring(0, 255) : req.getDescription());
        p.setSidebarMenuKey(normalizeSidebarMenuKey(req.getSidebarMenuKey()));
    }

    @Transactional
    public void delete(UUID id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy quyền");
        }
        permissionRepository.deleteById(id);
    }

    private PermissionResponse toResponse(Permission p) {
        String sk = p.getSidebarMenuKey();
        String label = SidebarMenuDefinition.fromMenuKey(sk).map(SidebarMenuDefinition::getLabelVi).orElse(null);
        return new PermissionResponse(p.getId(), p.getCode(), p.getName(), p.getDescription(), sk, label);
    }

    private static String normalizeSidebarMenuKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String k = raw.trim();
        if (k.length() > 64) {
            k = k.substring(0, 64);
        }
        if (!SidebarMenuDefinition.isValidMenuKeyOrBlank(k)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mục sidebar không hợp lệ");
        }
        return SidebarMenuDefinition.fromMenuKey(k).map(SidebarMenuDefinition::getMenuKey).orElse(null);
    }
}

package com.example.stduents_management.permission.controller;

import com.example.stduents_management.permission.dto.PermissionRequest;
import com.example.stduents_management.permission.dto.PermissionResponse;
import com.example.stduents_management.permission.dto.RolePermissionRequest;
import com.example.stduents_management.permission.dto.RolePermissionResponse;
import com.example.stduents_management.permission.service.PermissionService;
import com.example.stduents_management.permission.service.RolePermissionService;
import com.example.stduents_management.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionDashboardController {

    private final RolePermissionService rolePermissionService;
    private final PermissionService permissionService;
    private final RoleService roleService;

    // ---------- Phân quyền (Role-Permission assignments) ----------
    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<RolePermissionResponse> assignments = rolePermissionService.search(keyword, page, size);
        model.addAttribute("assignments", assignments);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "permissions/index";
    }

    @GetMapping("/new")
    public String newAssignmentForm(Model model) {
        model.addAttribute("request", new RolePermissionRequest());
        loadAssignmentFormData(model);
        return "permissions/form-assignment";
    }

    @PostMapping
    public String createAssignment(
            @Valid @ModelAttribute("request") RolePermissionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            loadAssignmentFormData(model);
            return "permissions/form-assignment";
        }
        try {
            rolePermissionService.create(req);
            redirect.addFlashAttribute("success", "Gán quyền thành công");
            return "redirect:/admin/permissions";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            loadAssignmentFormData(model);
            return "permissions/form-assignment";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            rolePermissionService.delete(id);
            redirect.addFlashAttribute("success", "Đã bỏ gán quyền");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/permissions";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("assignments", rolePermissionService.getForPrint());
        return "permissions/print";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        try {
            rolePermissionService.importExcel(file);
            redirect.addFlashAttribute("success", "Import phân quyền thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/permissions";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = rolePermissionService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=role-permissions.xlsx");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private void loadAssignmentFormData(Model model) {
        model.addAttribute("roles", roleService.getAll());
        model.addAttribute("permissions", permissionService.findAll());
    }

    // ---------- Định nghĩa quyền (Permission CRUD) ----------
    @GetMapping("/definitions")
    public String definitionsIndex(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<PermissionResponse> permissions = permissionService.search(keyword, page, size);
        model.addAttribute("permissions", permissions);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "permissions/definitions-index";
    }

    @GetMapping("/definitions/new")
    public String newPermissionForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("permissionRequest", new PermissionRequest());
        return "permissions/definitions-form";
    }

    @PostMapping("/definitions")
    public String createPermission(
            @Valid @ModelAttribute("permissionRequest") PermissionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "permissions/definitions-form";
        }
        try {
            permissionService.create(req);
            redirect.addFlashAttribute("success", "Thêm quyền thành công");
            return "redirect:/admin/permissions/definitions";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getReason());
            return "permissions/definitions-form";
        }
    }

    @GetMapping("/definitions/{id}/edit")
    public String editPermissionForm(@PathVariable UUID id, Model model) {
        PermissionResponse r = permissionService.getById(id);
        PermissionRequest req = new PermissionRequest();
        req.setCode(r.code());
        req.setName(r.name());
        req.setDescription(r.description());
        model.addAttribute("mode", "edit");
        model.addAttribute("permissionId", id);
        model.addAttribute("permissionRequest", req);
        return "permissions/definitions-form";
    }

    @PostMapping("/definitions/{id}")
    public String updatePermission(
            @PathVariable UUID id,
            @Valid @ModelAttribute("permissionRequest") PermissionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("permissionId", id);
            return "permissions/definitions-form";
        }
        try {
            permissionService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật quyền thành công");
            return "redirect:/admin/permissions/definitions";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("permissionId", id);
            model.addAttribute("globalError", e.getReason());
            return "permissions/definitions-form";
        }
    }

    @PostMapping("/definitions/{id}/delete")
    public String deletePermission(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            permissionService.delete(id);
            redirect.addFlashAttribute("success", "Xóa quyền thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/permissions/definitions";
    }
}

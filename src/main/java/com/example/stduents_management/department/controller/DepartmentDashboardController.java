package com.example.stduents_management.department.controller;

import com.example.stduents_management.department.dto.DepartmentRequest;
import com.example.stduents_management.department.dto.DepartmentResponse;
import com.example.stduents_management.department.service.DepartmentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/departments")
@RequiredArgsConstructor
public class DepartmentDashboardController {

    private final DepartmentService departmentService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<DepartmentResponse> departments = departmentService.search(keyword, page, size);
        model.addAttribute("departments", departments);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "departments/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("departmentRequest", new DepartmentRequest());
        return "departments/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute DepartmentRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "departments/form";
        }
        try {
            departmentService.create(request);
            redirect.addFlashAttribute("success", "Thêm phòng ban thành công");
            return "redirect:/admin/departments";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getReason());
            return "departments/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        DepartmentResponse d = departmentService.getById(id);
        DepartmentRequest req = new DepartmentRequest();
        req.setDepartmentCode(d.getDepartmentCode());
        req.setDepartmentName(d.getDepartmentName());
        req.setDescription(d.getDescription());

        model.addAttribute("mode", "edit");
        model.addAttribute("departmentId", id);
        model.addAttribute("departmentRequest", req);
        return "departments/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute DepartmentRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("departmentId", id);
            return "departments/form";
        }
        try {
            departmentService.update(id, request);
            redirect.addFlashAttribute("success", "Cập nhật phòng ban thành công");
            return "redirect:/admin/departments";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("departmentId", id);
            model.addAttribute("globalError", e.getReason());
            return "departments/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable UUID id,
            RedirectAttributes redirect
    ) {
        try {
            departmentService.delete(id);
            redirect.addFlashAttribute("success", "Xóa phòng ban thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        departmentService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            int count = departmentService.importExcel(file);
            redirect.addFlashAttribute("success", "Đã import " + count + " phòng ban");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/departments";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("departments", departmentService.getForPrint());
        return "departments/print";
    }
}

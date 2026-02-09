package com.example.stduents_management.educationtype.controller;

import com.example.stduents_management.educationtype.dto.EducationTypeRequest;
import com.example.stduents_management.educationtype.dto.EducationTypeResponse;
import com.example.stduents_management.educationtype.service.EducationTypeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/education-types")
@RequiredArgsConstructor
public class EducationTypeDashboardController {

    private final EducationTypeService service;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<EducationTypeResponse> list =
                service.search(keyword, page, size);

        model.addAttribute("educationTypes", list);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "education-types/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("educationTypeRequest",
                new EducationTypeRequest());
        return "education-types/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("educationTypeRequest") EducationTypeRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "create");
        model.addAttribute("educationTypeId", null);

        if (bindingResult.hasErrors()) {
            return "education-types/form";
        }

        try {
            service.create(request);
            return "redirect:/admin/education-types";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            return "education-types/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        EducationTypeResponse e = service.getById(id);

        EducationTypeRequest req = new EducationTypeRequest();
        req.setEducationTypeName(e.getEducationTypeName());
        req.setIsActive(e.getIsActive());

        model.addAttribute("mode", "edit");
        model.addAttribute("educationTypeId", id);
        model.addAttribute("educationTypeRequest", req);
        return "education-types/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("educationTypeRequest") EducationTypeRequest request,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "edit");
        model.addAttribute("educationTypeId", id);

        if (bindingResult.hasErrors()) {
            return "education-types/form";
        }

        try {
            service.update(id, request);
            return "redirect:/admin/education-types";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            return "education-types/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        service.delete(id);
        return "redirect:/admin/education-types";
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        service.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            int count = service.importExcel(file);
            redirect.addFlashAttribute(
                    "success", "Đã import " + count + " hệ đào tạo"
            );
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/education-types";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute(
                "educationTypes",
                service.getForPrint()
        );
        return "education-types/print";
    }
}

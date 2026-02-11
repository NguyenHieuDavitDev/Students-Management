package com.example.stduents_management.position.controller;

import com.example.stduents_management.position.dto.PositionRequest;
import com.example.stduents_management.position.dto.PositionResponse;
import com.example.stduents_management.position.service.PositionService;
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
@RequestMapping("/admin/positions")
@RequiredArgsConstructor
public class PositionDashboardController {

    private final PositionService positionService;

    /* ================= LIST ================= */
    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        Page<PositionResponse> positions =
                positionService.search(keyword, page, size);

        model.addAttribute("positions", positions);
        model.addAttribute("keyword",
                keyword == null ? "" : keyword);

        return "positions/index";
    }

    /* ================= CREATE FORM ================= */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("positionRequest",
                new PositionRequest());
        return "positions/form";
    }

    /* ================= CREATE ================= */
    @PostMapping
    public String create(
            @Valid @ModelAttribute PositionRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "positions/form";
        }

        try {
            positionService.create(request);
            redirect.addFlashAttribute("success", "Thêm chức danh thành công");
            return "redirect:/admin/positions";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getReason());
            return "positions/form";
        }
    }

    /* ================= EDIT FORM ================= */
    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable UUID id,
            Model model
    ) {

        PositionResponse p =
                positionService.getById(id);

        PositionRequest req = new PositionRequest();
        req.setPositionCode(p.getPositionCode());
        req.setPositionName(p.getPositionName());
        req.setDescription(p.getDescription());

        model.addAttribute("mode", "edit");
        model.addAttribute("positionId", id);
        model.addAttribute("positionRequest", req);

        return "positions/form";
    }

    /* ================= UPDATE ================= */
    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute PositionRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("positionId", id);
            return "positions/form";
        }

        try {
            positionService.update(id, request);
            redirect.addFlashAttribute("success", "Cập nhật chức danh thành công");
            return "redirect:/admin/positions";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("positionId", id);
            model.addAttribute("globalError", e.getReason());
            return "positions/form";
        }
    }

    /* ================= DELETE ================= */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable UUID id,
            RedirectAttributes redirect
    ) {
        try {
            positionService.delete(id);
            redirect.addFlashAttribute("success", "Xóa chức danh thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/positions";
    }

    /* ================= EXPORT ================= */
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        positionService.exportExcel(response);
    }

    /* ================= IMPORT ================= */
    @PostMapping("/import")
    public String importExcel(
            @RequestParam MultipartFile file,
            RedirectAttributes redirect
    ) {

        try {
            int count =
                    positionService.importExcel(file);

            redirect.addFlashAttribute(
                    "success",
                    "Đã import " + count + " chức danh");
        } catch (Exception e) {
            redirect.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/admin/positions";
    }

    /* ================= PRINT ================= */
    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute(
                "positions",
                positionService.getForPrint());
        return "positions/print";
    }
}


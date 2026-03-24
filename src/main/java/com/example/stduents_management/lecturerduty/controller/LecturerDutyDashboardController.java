package com.example.stduents_management.lecturerduty.controller;

import com.example.stduents_management.lecturerduty.dto.LecturerDutyRequest;
import com.example.stduents_management.lecturerduty.dto.LecturerDutyResponse;
import com.example.stduents_management.lecturerduty.service.LecturerDutyService;
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
@RequestMapping("/admin/lecturer-duties")
@RequiredArgsConstructor
public class LecturerDutyDashboardController {

    private final LecturerDutyService lecturerDutyService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        Page<LecturerDutyResponse> duties =
                lecturerDutyService.search(keyword, page, size);

        model.addAttribute("duties", duties);
        model.addAttribute("keyword",
                keyword == null ? "" : keyword);

        return "lecturer-duties/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("lecturerDutyRequest",
                new LecturerDutyRequest());
        return "lecturer-duties/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute LecturerDutyRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "lecturer-duties/form";
        }

        try {
            lecturerDutyService.create(request);
            redirect.addFlashAttribute("success", "Thêm chức vụ thành công");
            return "redirect:/admin/lecturer-duties";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getReason());
            return "lecturer-duties/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable UUID id,
            Model model
    ) {

        LecturerDutyResponse d =
                lecturerDutyService.getById(id);

        LecturerDutyRequest req = new LecturerDutyRequest();
        req.setDutyCode(d.dutyCode());
        req.setDutyName(d.dutyName());
        req.setDescription(d.description());

        model.addAttribute("mode", "edit");
        model.addAttribute("lecturerDutyId", id);
        model.addAttribute("lecturerDutyRequest", req);

        return "lecturer-duties/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute LecturerDutyRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("lecturerDutyId", id);
            return "lecturer-duties/form";
        }

        try {
            lecturerDutyService.update(id, request);
            redirect.addFlashAttribute("success", "Cập nhật chức vụ thành công");
            return "redirect:/admin/lecturer-duties";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("lecturerDutyId", id);
            model.addAttribute("globalError", e.getReason());
            return "lecturer-duties/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable UUID id,
            RedirectAttributes redirect
    ) {
        try {
            lecturerDutyService.delete(id);
            redirect.addFlashAttribute("success", "Xóa chức vụ thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/lecturer-duties";
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        lecturerDutyService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam MultipartFile file,
            RedirectAttributes redirect
    ) {

        try {
            int count =
                    lecturerDutyService.importExcel(file);

            redirect.addFlashAttribute(
                    "success",
                    "Đã import " + count + " chức vụ");
        } catch (Exception e) {
            redirect.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/admin/lecturer-duties";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute(
                "duties",
                lecturerDutyService.getForPrint());
        return "lecturer-duties/print";
    }
}

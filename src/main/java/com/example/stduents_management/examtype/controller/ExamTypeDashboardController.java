package com.example.stduents_management.examtype.controller;

import com.example.stduents_management.examtype.dto.ExamTypeRequest;
import com.example.stduents_management.examtype.dto.ExamTypeResponse;
import com.example.stduents_management.examtype.service.ExamTypeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/exam-types")
@RequiredArgsConstructor
public class ExamTypeDashboardController {

    private final ExamTypeService examTypeService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Page<ExamTypeResponse> items = examTypeService.search(keyword, fromDate, toDate, page, size);

        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("fromDate", fromDate == null ? "" : fromDate);
        model.addAttribute("toDate", toDate == null ? "" : toDate);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "exam-types/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("examTypeRequest", new ExamTypeRequest());
        return "exam-types/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("examTypeRequest") ExamTypeRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "exam-types/form";
        }
        try {
            examTypeService.create(req);
            redirect.addFlashAttribute("success", "Thêm loại kỳ thi thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getMessage());
            return "exam-types/form";
        }
        return "redirect:/admin/exam-types";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        ExamTypeResponse r = examTypeService.getById(id);
        ExamTypeRequest req = new ExamTypeRequest();
        req.setName(r.name());
        req.setDescription(r.description());

        model.addAttribute("mode", "edit");
        model.addAttribute("examTypeId", id);
        model.addAttribute("examTypeRequest", req);
        model.addAttribute("currentItem", r);
        return "exam-types/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("examTypeRequest") ExamTypeRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("examTypeId", id);
            return "exam-types/form";
        }
        try {
            examTypeService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật loại kỳ thi thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("examTypeId", id);
            model.addAttribute("globalError", e.getMessage());
            return "exam-types/form";
        }
        return "redirect:/admin/exam-types";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            examTypeService.delete(id);
            redirect.addFlashAttribute("success", "Xóa loại kỳ thi thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/exam-types";
    }

    @GetMapping("/print")
    public String print(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model
    ) {
        List<ExamTypeResponse> items = examTypeService.getAllFiltered(keyword, fromDate, toDate);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("fromDate", fromDate == null ? "" : fromDate);
        model.addAttribute("toDate", toDate == null ? "" : toDate);
        return "exam-types/print";
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        examTypeService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            int count = examTypeService.importExcel(file);
            redirect.addFlashAttribute("success",
                    "Import thành công " + count + " loại kỳ thi");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
        }
        return "redirect:/admin/exam-types";
    }
}

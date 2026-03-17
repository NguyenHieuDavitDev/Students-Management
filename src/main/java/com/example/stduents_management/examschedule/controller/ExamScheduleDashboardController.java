package com.example.stduents_management.examschedule.controller;

import com.example.stduents_management.examschedule.dto.ExamScheduleRequest;
import com.example.stduents_management.examschedule.dto.ExamScheduleResponse;
import com.example.stduents_management.examschedule.service.ExamScheduleService;
import com.example.stduents_management.classsection.service.ClassSectionService;
import com.example.stduents_management.examtype.service.ExamTypeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/exam-schedules")
@RequiredArgsConstructor
public class ExamScheduleDashboardController {

    private final ExamScheduleService examScheduleService;
    private final ClassSectionService classSectionService;
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
        Page<ExamScheduleResponse> items =
                examScheduleService.search(keyword, fromDate, toDate, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("fromDate", fromDate == null ? "" : fromDate);
        model.addAttribute("toDate", toDate == null ? "" : toDate);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "exam-schedules/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("examScheduleRequest", new ExamScheduleRequest());
        loadSelectData(model);
        return "exam-schedules/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("examScheduleRequest") ExamScheduleRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            return "exam-schedules/form";
        }
        try {
            examScheduleService.create(req);
            redirect.addFlashAttribute("success", "Thêm lịch thi thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            model.addAttribute("globalError", e.getMessage());
            return "exam-schedules/form";
        }
        return "redirect:/admin/exam-schedules";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        ExamScheduleResponse r = examScheduleService.getById(id);
        ExamScheduleRequest req = new ExamScheduleRequest();
        req.setClassSectionId(r.classSectionId());
        req.setExamTypeId(r.examTypeId());
        req.setExamDate(r.examDate());
        req.setStartTime(r.startTime());
        req.setDurationMinutes(r.durationMinutes());
        req.setNote(r.note());

        model.addAttribute("mode", "edit");
        model.addAttribute("examScheduleId", id);
        model.addAttribute("examScheduleRequest", req);
        model.addAttribute("currentItem", r);
        loadSelectData(model);
        return "exam-schedules/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("examScheduleRequest") ExamScheduleRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("examScheduleId", id);
            loadSelectData(model);
            return "exam-schedules/form";
        }
        try {
            examScheduleService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật lịch thi thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("examScheduleId", id);
            loadSelectData(model);
            model.addAttribute("globalError", e.getMessage());
            return "exam-schedules/form";
        }
        return "redirect:/admin/exam-schedules";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            examScheduleService.delete(id);
            redirect.addFlashAttribute("success", "Xóa lịch thi thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/exam-schedules";
    }

    @GetMapping("/print")
    public String print(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model
    ) {
        List<ExamScheduleResponse> items =
                examScheduleService.getAllFiltered(keyword, fromDate, toDate);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("fromDate", fromDate == null ? "" : fromDate);
        model.addAttribute("toDate", toDate == null ? "" : toDate);
        return "exam-schedules/print";
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        examScheduleService.exportExcel(response);
    }

    private void loadSelectData(Model model) {
        // giả sử ClassSectionService có phương thức lấy tất cả / hoặc theo học kỳ hiện tại
        model.addAttribute("classSections",
                classSectionService.search(null, 0, Integer.MAX_VALUE).getContent());
        model.addAttribute("examTypes", examTypeService.getAll());
    }
}
package com.example.stduents_management.trainingprogram.controller;

import com.example.stduents_management.major.service.MajorService;
import com.example.stduents_management.trainingprogram.dto.TrainingProgramRequest;
import com.example.stduents_management.trainingprogram.dto.TrainingProgramResponse;
import com.example.stduents_management.trainingprogram.service.TrainingProgramService;
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
@RequestMapping("/admin/training-programs")
@RequiredArgsConstructor
public class TrainingProgramDashboardController {

    private final TrainingProgramService trainingProgramService;
    private final MajorService majorService;

    /* ================= LIST ================= */
    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        Page<TrainingProgramResponse> programs =
                trainingProgramService.search(keyword, page, size);

        model.addAttribute("programs", programs);
        model.addAttribute("keyword",
                keyword == null ? "" : keyword);

        return "training-programs/index";
    }

    /* ================= CREATE FORM ================= */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("programRequest",
                new TrainingProgramRequest());
        model.addAttribute("majors",
                majorService.getForPrint());
        return "training-programs/form";
    }

    /* ================= CREATE ================= */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("programRequest") TrainingProgramRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("majors",
                    majorService.getForPrint());
            return "training-programs/form";
        }

        try {
            trainingProgramService.create(request);
            redirect.addFlashAttribute("success", "Thêm chương trình đào tạo thành công");
            return "redirect:/admin/training-programs";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("majors",
                    majorService.getForPrint());
            model.addAttribute("globalError", e.getReason());
            return "training-programs/form";
        }
    }

    /* ================= EDIT FORM ================= */
    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable UUID id,
            Model model
    ) {

        TrainingProgramResponse p =
                trainingProgramService.getById(id);

        TrainingProgramRequest req = new TrainingProgramRequest();
        req.setProgramCode(p.getProgramCode());
        req.setProgramName(p.getProgramName());
        req.setMajorId(p.getMajorId());
        req.setCourse(p.getCourse());
        req.setDescription(p.getDescription());
        req.setDurationYears(p.getDurationYears());
        req.setTotalCredits(p.getTotalCredits());
        req.setIsActive(p.getIsActive());

        model.addAttribute("mode", "edit");
        model.addAttribute("programId", id);
        model.addAttribute("programRequest", req);
        model.addAttribute("majors",
                majorService.getForPrint());

        return "training-programs/form";
    }

    /* ================= UPDATE ================= */
    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("programRequest") TrainingProgramRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("programId", id);
            model.addAttribute("majors",
                    majorService.getForPrint());
            return "training-programs/form";
        }

        try {
            trainingProgramService.update(id, request);
            redirect.addFlashAttribute("success", "Cập nhật chương trình đào tạo thành công");
            return "redirect:/admin/training-programs";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("programId", id);
            model.addAttribute("majors",
                    majorService.getForPrint());
            model.addAttribute("globalError", e.getReason());
            return "training-programs/form";
        }
    }

    /* ================= DELETE ================= */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable UUID id,
            RedirectAttributes redirect
    ) {
        try {
            trainingProgramService.delete(id);
            redirect.addFlashAttribute("success", "Xóa chương trình đào tạo thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/training-programs";
    }

    /* ================= EXPORT ================= */
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        trainingProgramService.exportExcel(response);
    }

    /* ================= IMPORT ================= */
    @PostMapping("/import")
    public String importExcel(
            @RequestParam MultipartFile file,
            RedirectAttributes redirect
    ) {

        try {
            int count =
                    trainingProgramService.importExcel(file);

            redirect.addFlashAttribute(
                    "success",
                    "Đã import " + count + " chương trình đào tạo");
        } catch (Exception e) {
            redirect.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/admin/training-programs";
    }

    /* ================= PRINT ================= */
    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute(
                "programs",
                trainingProgramService.getForPrint());
        return "training-programs/print";
    }
}

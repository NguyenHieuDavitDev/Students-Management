package com.example.stduents_management.traininglevel.controller;

import com.example.stduents_management.traininglevel.dto.TrainingLevelRequest;
import com.example.stduents_management.traininglevel.dto.TrainingLevelResponse;
import com.example.stduents_management.traininglevel.service.TrainingLevelService;
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
@RequestMapping("/admin/training-levels")
@RequiredArgsConstructor
public class TrainingLevelDashboardController {

    private final TrainingLevelService trainingLevelService;

    /* ===================== LIST ===================== */
    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<TrainingLevelResponse> levels =
                trainingLevelService.search(keyword, page, size);

        model.addAttribute("trainingLevels", levels);
        model.addAttribute("keyword", keyword);


        model.addAttribute("activeMenu", "trainingLevels");

        return "training-levels/list";
    }

    /* ===================== CREATE FORM ===================== */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("trainingLevelRequest", new TrainingLevelRequest());


        model.addAttribute("activeMenu", "trainingLevels");

        return "training-levels/form";
    }

    /* ===================== CREATE ===================== */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("trainingLevelRequest") TrainingLevelRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("activeMenu", "trainingLevels");
            return "training-levels/form";
        }

        try {
            trainingLevelService.create(request);
            return "redirect:/admin/training-levels";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            model.addAttribute("mode", "create");
            model.addAttribute("activeMenu", "trainingLevels");
            return "training-levels/form";
        }
    }

    /* ===================== EDIT FORM ===================== */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        TrainingLevelResponse level =
                trainingLevelService.getById(id);

        TrainingLevelRequest request = new TrainingLevelRequest();
        request.setTrainingLevelName(level.getTrainingLevelName());
        request.setDescription(level.getDescription());

        model.addAttribute("trainingLevelRequest", request);
        model.addAttribute("trainingLevelId", id);
        model.addAttribute("mode", "edit");
        model.addAttribute("activeMenu", "trainingLevels");

        return "training-levels/form";
    }

    /* ===================== UPDATE ===================== */
    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("trainingLevelRequest") TrainingLevelRequest request,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("trainingLevelId", id);
            model.addAttribute("activeMenu", "trainingLevels");
            return "training-levels/form";
        }

        try {
            trainingLevelService.update(id, request);
            return "redirect:/admin/training-levels";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            model.addAttribute("mode", "edit");
            model.addAttribute("trainingLevelId", id);
            model.addAttribute("activeMenu", "trainingLevels");
            return "training-levels/form";
        }
    }

    /* ===================== DELETE ===================== */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        trainingLevelService.delete(id);
        return "redirect:/admin/training-levels";
    }

    /* ===================== EXPORT ===================== */
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        trainingLevelService.exportExcel(response);
    }

    /* ===================== IMPORT ===================== */
    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            int count = trainingLevelService.importExcel(file);
            redirect.addFlashAttribute(
                    "success",
                    "Đã import " + count + " trình độ đào tạo"
            );
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/training-levels";
    }

    /* ===================== PRINT ===================== */
    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute(
                "trainingLevels",
                trainingLevelService.getForPrint()
        );

        // Không bắt buộc nhưng nên có
        model.addAttribute("activeMenu", "trainingLevels");

        return "training-levels/print";
    }
}

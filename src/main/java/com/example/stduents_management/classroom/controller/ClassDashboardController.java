package com.example.stduents_management.classroom.controller;

import com.example.stduents_management.classroom.dto.ClassRequest;
import com.example.stduents_management.classroom.dto.ClassResponse;
import com.example.stduents_management.classroom.service.ClassService;
import com.example.stduents_management.educationtype.repository.EducationTypeRepository;
import com.example.stduents_management.major.repository.MajorRepository;
import com.example.stduents_management.traininglevel.repository.TrainingLevelRepository;
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
@RequestMapping("/admin/classes")
@RequiredArgsConstructor
public class ClassDashboardController {

    private final ClassService classService;
    private final MajorRepository majorRepository;
    private final EducationTypeRepository educationTypeRepository;
    private final TrainingLevelRepository trainingLevelRepository;

    /* ================== LIST + SEARCH ================== */
    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<ClassResponse> classes =
                classService.search(keyword, page, size);

        model.addAttribute("classes", classes);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "classes/index";
    }

    /* ================== CREATE FORM ================== */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("classRequest", new ClassRequest());
        loadSelectData(model);
        return "classes/form";
    }

    /* ================== CREATE ================== */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("classRequest") ClassRequest classRequest,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "create");
        model.addAttribute("classId", null);

        if (bindingResult.hasErrors()) {
            loadSelectData(model);
            return "classes/form";
        }

        try {
            classService.create(classRequest);
            return "redirect:/admin/classes";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            loadSelectData(model);
            return "classes/form";
        }
    }

    /* ================== EDIT FORM ================== */
    @GetMapping("/{id}/edit")
    public String edit(
            @PathVariable UUID id,
            Model model
    ) {
        ClassResponse c = classService.getById(id);

        ClassRequest req = new ClassRequest();
        req.setClassCode(c.getClassCode());
        req.setClassName(c.getClassName());
        req.setAcademicYear(c.getAcademicYear());
        req.setMajorId(c.getMajorId());
        req.setEducationTypeId(c.getEducationTypeId());
        req.setTrainingLevelId(c.getTrainingLevelId());
        req.setMaxStudent(c.getMaxStudent());
        req.setClassStatus(c.getClassStatus());
        req.setIsActive(c.getIsActive());

        model.addAttribute("mode", "edit");
        model.addAttribute("classId", id);
        model.addAttribute("classRequest", req);
        loadSelectData(model);
        return "classes/form";
    }

    /* ================== UPDATE ================== */
    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("classRequest") ClassRequest classRequest,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "edit");
        model.addAttribute("classId", id);

        if (bindingResult.hasErrors()) {
            loadSelectData(model);
            return "classes/form";
        }

        try {
            classService.update(id, classRequest);
            return "redirect:/admin/classes";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            loadSelectData(model);
            return "classes/form";
        }
    }

    /* ================== DELETE ================== */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        classService.delete(id);
        return "redirect:/admin/classes";
    }

    /* ================== EXPORT ================== */
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        classService.exportExcel(response);
    }

    /* ================== IMPORT ================== */
    @PostMapping("/import")
    public String importExcel(
            @RequestParam MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            int count = classService.importExcel(file);
            redirect.addFlashAttribute(
                    "success", "Đã import " + count + " lớp học"
            );
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/classes";
    }

    /* ================== PRINT ================== */
    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute(
                "classes",
                classService.getForPrint()
        );
        return "classes/print";
    }

    /* ================== UTIL ================== */
    private void loadSelectData(Model model) {
        model.addAttribute("majors", majorRepository.findAll());
        model.addAttribute("educationTypes", educationTypeRepository.findAll());
        model.addAttribute("trainingLevels", trainingLevelRepository.findAll());
    }
}

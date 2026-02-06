package com.example.stduents_management.major.controller;

import com.example.stduents_management.faculty.repository.FacultyRepository;
import com.example.stduents_management.major.dto.MajorRequest;
import com.example.stduents_management.major.dto.MajorResponse;
import com.example.stduents_management.major.service.MajorService;
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
@RequestMapping("/admin/majors")
@RequiredArgsConstructor
public class MajorDashboardController {

    private final MajorService majorService;
    private final FacultyRepository facultyRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<MajorResponse> majors = majorService.search(keyword, page, size);

        model.addAttribute("majors", majors);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "majors/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("majorRequest", new MajorRequest());
        model.addAttribute("faculties", facultyRepository.findAll());
        return "majors/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute MajorRequest majorRequest,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("faculties", facultyRepository.findAll());
            return "majors/form";
        }

        try {
            majorService.create(majorRequest);
            return "redirect:/admin/majors";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            model.addAttribute("faculties", facultyRepository.findAll());
            return "majors/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        MajorResponse m = majorService.getById(id);

        MajorRequest req = new MajorRequest();
        req.setMajorName(m.getMajorName());
        req.setFacultyId(m.getFacultyId());

        model.addAttribute("mode", "edit");
        model.addAttribute("majorId", id);
        model.addAttribute("majorRequest", req);
        model.addAttribute("faculties", facultyRepository.findAll());
        return "majors/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute MajorRequest majorRequest,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("faculties", facultyRepository.findAll());
            return "majors/form";
        }

        try {
            majorService.update(id, majorRequest);
            return "redirect:/admin/majors";
        } catch (ResponseStatusException e) {
            model.addAttribute("globalError", e.getReason());
            model.addAttribute("faculties", facultyRepository.findAll());
            return "majors/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        majorService.delete(id);
        return "redirect:/admin/majors";
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        majorService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            int count = majorService.importExcel(file);
            redirect.addFlashAttribute(
                    "success", "Đã import " + count + " ngành"
            );
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/majors";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("majors", majorService.getForPrint());
        return "majors/print";
    }
}

package com.example.stduents_management.faculty.controller;

import com.example.stduents_management.faculty.dto.FacultyRequest;
import com.example.stduents_management.faculty.dto.FacultyResponse;
import com.example.stduents_management.faculty.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("/admin/faculties")
@RequiredArgsConstructor
public class FacultyDashboardController {

    private final FacultyService facultyService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<FacultyResponse> faculties =
                facultyService.search(keyword, page, size);

        model.addAttribute("faculties", faculties);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "faculties/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("facultyId", null);
        model.addAttribute("facultyRequest", new FacultyRequest());
        return "faculties/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute FacultyRequest facultyRequest,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "create");

        if (bindingResult.hasErrors()) {
            return "faculties/form";
        }

        try {
            facultyService.create(facultyRequest);
            return "redirect:/admin/faculties";
        } catch (ResponseStatusException ex) {
            model.addAttribute("globalError", ex.getReason());
            return "faculties/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable UUID id,
            Model model
    ) {
        FacultyResponse faculty = facultyService.getById(id);

        FacultyRequest req = new FacultyRequest();
        req.setFacultyCode(faculty.getFacultyCode());
        req.setFacultyName(faculty.getFacultyName());

        model.addAttribute("mode", "edit");
        model.addAttribute("facultyId", id);
        model.addAttribute("facultyRequest", req);
        return "faculties/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute FacultyRequest facultyRequest,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "edit");

        if (bindingResult.hasErrors()) {
            return "faculties/form";
        }

        try {
            facultyService.update(id, facultyRequest);
            return "redirect:/admin/faculties";
        } catch (ResponseStatusException ex) {
            model.addAttribute("globalError", ex.getReason());
            return "faculties/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        facultyService.delete(id);
        return "redirect:/admin/faculties";
    }
}

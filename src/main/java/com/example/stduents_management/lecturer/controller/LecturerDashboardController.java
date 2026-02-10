package com.example.stduents_management.lecturer.controller;

import com.example.stduents_management.faculty.repository.FacultyRepository;
import com.example.stduents_management.lecturer.dto.LecturerRequest;
import com.example.stduents_management.lecturer.dto.LecturerResponse;
import com.example.stduents_management.lecturer.service.LecturerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/lecturers")
@RequiredArgsConstructor
public class LecturerDashboardController {

    private final LecturerService lecturerService;
    private final FacultyRepository facultyRepository;

    /* ===================== LIST + SEARCH + PAGING ===================== */
    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<LecturerResponse> lecturers =
                lecturerService.search(keyword, page, size);

        model.addAttribute("lecturers", lecturers);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "lecturers/index";
    }

    /* ===================== CREATE FORM ===================== */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("lecturerRequest", new LecturerRequest());
        model.addAttribute("faculties", facultyRepository.findAll());
        return "lecturers/form";
    }

    /* ===================== CREATE ===================== */
    @PostMapping
    public String create(
            @Valid @ModelAttribute("lecturerRequest") LecturerRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("faculties", facultyRepository.findAll());
            return "lecturers/form";
        }

        lecturerService.create(req);
        redirect.addFlashAttribute("success", "Thêm giảng viên thành công");
        return "redirect:/admin/lecturers";
    }

    /* ===================== EDIT FORM ===================== */
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable UUID id,
            Model model
    ) {
        LecturerResponse l = lecturerService.getById(id);

        LecturerRequest req = new LecturerRequest();
        req.setLecturerCode(l.lecturerCode());
        req.setFullName(l.fullName());
        req.setDateOfBirth(l.dateOfBirth());
        req.setGender(l.gender());
        req.setCitizenId(l.citizenId());
        req.setEmail(l.email());
        req.setPhoneNumber(l.phoneNumber());
        req.setAddress(l.address());
        req.setAvatar(l.avatar());
        req.setAcademicDegree(l.academicDegree());
        req.setAcademicTitle(l.academicTitle());
        req.setFacultyId(l.facultyId());

        model.addAttribute("mode", "edit");
        model.addAttribute("lecturerId", id);
        model.addAttribute("lecturerRequest", req);
        model.addAttribute("faculties", facultyRepository.findAll());

        return "lecturers/form";
    }

    /* ===================== UPDATE ===================== */
    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("lecturerRequest") LecturerRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("lecturerId", id);
            model.addAttribute("faculties", facultyRepository.findAll());
            return "lecturers/form";
        }

        lecturerService.update(id, req);
        redirect.addFlashAttribute("success", "Cập nhật giảng viên thành công");
        return "redirect:/admin/lecturers";
    }

    /* ===================== DELETE ===================== */
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable UUID id,
            RedirectAttributes redirect
    ) {
        lecturerService.delete(id);
        redirect.addFlashAttribute("success", "Xóa giảng viên thành công");
        return "redirect:/admin/lecturers";
    }

    /* ===================== PRINT ===================== */
    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("lecturers", lecturerService.getForPrint());
        return "lecturers/print";
    }
}

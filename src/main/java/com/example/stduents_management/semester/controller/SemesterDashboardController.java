package com.example.stduents_management.semester.controller;

import com.example.stduents_management.semester.dto.SemesterRequest;
import com.example.stduents_management.semester.dto.SemesterResponse;
import com.example.stduents_management.semester.entity.SemesterStatus;
import com.example.stduents_management.semester.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/semesters")
@RequiredArgsConstructor
public class SemesterDashboardController {

    private final SemesterService semesterService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<SemesterResponse> semesters = semesterService.search(keyword, page, size);

        model.addAttribute("semesters", semesters);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "semesters/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        SemesterRequest req = new SemesterRequest();
        req.setStatus(SemesterStatus.UPCOMING);
        req.setTerm(1);

        model.addAttribute("mode", "create");
        model.addAttribute("semesterRequest", req);
        model.addAttribute("statuses", SemesterStatus.values());
        return "semesters/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("semesterRequest") SemesterRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("statuses", SemesterStatus.values());
            return "semesters/form";
        }

        try {
            semesterService.create(req);
            redirect.addFlashAttribute("success", "Thêm học kỳ thành công");
            return "redirect:/admin/semesters";
        } catch (ResponseStatusException e) {
            model.addAttribute("error", e.getReason());
            model.addAttribute("mode", "create");
            model.addAttribute("statuses", SemesterStatus.values());
            return "semesters/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model
    ) {
        SemesterResponse s = semesterService.getById(id);

        SemesterRequest req = new SemesterRequest();
        req.setCode(s.code());
        req.setName(s.name());
        req.setAcademicYear(s.academicYear());
        req.setTerm(s.term());
        req.setStartDate(s.startDate());
        req.setEndDate(s.endDate());
        req.setRegistrationStart(s.registrationStart());
        req.setRegistrationEnd(s.registrationEnd());
        req.setStatus(s.status());
        req.setDescription(s.description());

        model.addAttribute("mode", "edit");
        model.addAttribute("semesterId", id);
        model.addAttribute("semesterRequest", req);
        model.addAttribute("statuses", SemesterStatus.values());

        return "semesters/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("semesterRequest") SemesterRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("semesterId", id);
            model.addAttribute("statuses", SemesterStatus.values());
            return "semesters/form";
        }

        try {
            semesterService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật học kỳ thành công");
            return "redirect:/admin/semesters";
        } catch (ResponseStatusException e) {
            model.addAttribute("error", e.getReason());
            model.addAttribute("mode", "edit");
            model.addAttribute("semesterId", id);
            model.addAttribute("statuses", SemesterStatus.values());
            return "semesters/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirect
    ) {
        try {
            semesterService.delete(id);
            redirect.addFlashAttribute("success", "Xóa học kỳ thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/semesters";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("semesters", semesterService.getForPrint());
        return "semesters/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            semesterService.importExcel(file);
            redirect.addFlashAttribute("success", "Import học kỳ thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/semesters";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {

        byte[] data = semesterService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=semesters.xlsx");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
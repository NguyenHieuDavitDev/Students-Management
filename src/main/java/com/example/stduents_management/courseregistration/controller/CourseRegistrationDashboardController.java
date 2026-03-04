package com.example.stduents_management.courseregistration.controller;

import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.courseregistration.dto.CourseRegistrationRequest;
import com.example.stduents_management.courseregistration.dto.CourseRegistrationResponse;
import com.example.stduents_management.courseregistration.service.CourseRegistrationService;
import com.example.stduents_management.student.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/course-registrations")
@RequiredArgsConstructor
public class CourseRegistrationDashboardController {

    private final CourseRegistrationService service;
    private final StudentRepository studentRepository;
    private final ClassSectionRepository classSectionRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<CourseRegistrationResponse> registrations = service.search(keyword, page, size);
        model.addAttribute("registrations", registrations);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "course-registrations/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("registrationRequest", new CourseRegistrationRequest());
        model.addAttribute("students", studentRepository.findAll(Sort.by("studentCode")));
        model.addAttribute("sections", classSectionRepository.findAll(Sort.by("classCode")));
        return "course-registrations/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("registrationRequest") CourseRegistrationRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("students", studentRepository.findAll(Sort.by("studentCode")));
            model.addAttribute("sections", classSectionRepository.findAll(Sort.by("classCode")));
            return "course-registrations/form";
        }
        try {
            service.create(req);
            redirect.addFlashAttribute("success", "Đăng ký học phần thành công");
            return "redirect:/admin/course-registrations";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("students", studentRepository.findAll(Sort.by("studentCode")));
            model.addAttribute("sections", classSectionRepository.findAll(Sort.by("classCode")));
            model.addAttribute("globalError", e.getReason());
            return "course-registrations/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        CourseRegistrationResponse r = service.getById(id);
        CourseRegistrationRequest req = new CourseRegistrationRequest();
        req.setStudentId(r.studentId());
        req.setClassSectionId(r.classSectionId());
        req.setNote(r.note());

        model.addAttribute("mode", "edit");
        model.addAttribute("registrationId", id);
        model.addAttribute("registrationRequest", req);
        model.addAttribute("students", studentRepository.findAll(Sort.by("studentCode")));
        model.addAttribute("sections", classSectionRepository.findAll(Sort.by("classCode")));
        return "course-registrations/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("registrationRequest") CourseRegistrationRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("registrationId", id);
            model.addAttribute("students", studentRepository.findAll(Sort.by("studentCode")));
            model.addAttribute("sections", classSectionRepository.findAll(Sort.by("classCode")));
            return "course-registrations/form";
        }
        try {
            service.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật đăng ký học phần thành công");
            return "redirect:/admin/course-registrations";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("registrationId", id);
            model.addAttribute("students", studentRepository.findAll(Sort.by("studentCode")));
            model.addAttribute("sections", classSectionRepository.findAll(Sort.by("classCode")));
            model.addAttribute("globalError", e.getReason());
            return "course-registrations/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("success", "Xóa đăng ký học phần thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/course-registrations";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("registrations", service.getForPrint());
        return "course-registrations/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            service.importExcel(file);
            redirect.addFlashAttribute("success", "Import đăng ký học phần thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/course-registrations";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = service.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_registrations.xlsx");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}


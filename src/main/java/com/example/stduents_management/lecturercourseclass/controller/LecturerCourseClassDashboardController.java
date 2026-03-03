package com.example.stduents_management.lecturercourseclass.controller;

import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.lecturercourseclass.dto.LecturerCourseClassRequest;
import com.example.stduents_management.lecturercourseclass.dto.LecturerCourseClassResponse;
import com.example.stduents_management.lecturercourseclass.service.LecturerCourseClassService;
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
@RequestMapping("/admin/lecturer-course-classes")
@RequiredArgsConstructor
public class LecturerCourseClassDashboardController {

    private final LecturerCourseClassService service;
    private final ClassSectionRepository classSectionRepository;
    private final LecturerRepository lecturerRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<LecturerCourseClassResponse> items = service.search(keyword, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "lecturer-course-classes/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("request", new LecturerCourseClassRequest());
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
        return "lecturer-course-classes/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("request") LecturerCourseClassRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
            model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
            return "lecturer-course-classes/form";
        }
        try {
            service.create(req);
            redirect.addFlashAttribute("success", "Phân công giảng viên thành công");
        } catch (ResponseStatusException e) {
            model.addAttribute("error", e.getReason());
            model.addAttribute("mode", "create");
            model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
            model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
            return "lecturer-course-classes/form";
        }
        return "redirect:/admin/lecturer-course-classes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        LecturerCourseClassResponse r = service.getById(id);
        LecturerCourseClassRequest req = new LecturerCourseClassRequest();
        req.setClassSectionId(r.classSectionId());
        req.setLecturerId(r.lecturerId());
        req.setNote(r.note());

        model.addAttribute("mode", "edit");
        model.addAttribute("itemId", id);
        model.addAttribute("request", req);
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
        return "lecturer-course-classes/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("request") LecturerCourseClassRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("itemId", id);
            model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
            model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
            return "lecturer-course-classes/form";
        }
        try {
            service.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật phân công giảng viên thành công");
        } catch (ResponseStatusException e) {
            model.addAttribute("error", e.getReason());
            model.addAttribute("mode", "edit");
            model.addAttribute("itemId", id);
            model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
            model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
            return "lecturer-course-classes/form";
        }
        return "redirect:/admin/lecturer-course-classes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("success", "Xóa phân công giảng viên thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/lecturer-course-classes";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("items", service.getForPrint());
        return "lecturer-course-classes/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            service.importExcel(file);
            redirect.addFlashAttribute("success", "Import phân công giảng viên thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/lecturer-course-classes";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = service.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lecturer_course_classes.xlsx");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}


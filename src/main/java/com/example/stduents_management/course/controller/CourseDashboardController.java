package com.example.stduents_management.course.controller;

import com.example.stduents_management.course.dto.CourseRequest;
import com.example.stduents_management.course.dto.CourseResponse;
import com.example.stduents_management.course.service.CourseService;
import com.example.stduents_management.faculty.service.FacultyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
public class CourseDashboardController {

    private final CourseService courseService;
    private final FacultyService facultyService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<CourseResponse> courses = courseService.search(keyword, page, size);
        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "courses/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        CourseRequest req = new CourseRequest();
        req.setStatus(true);

        model.addAttribute("mode", "create");
        model.addAttribute("courseRequest", req);
        model.addAttribute("faculties", facultyService.getForPrint());
        return "courses/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("courseRequest") CourseRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("faculties", facultyService.getForPrint());
            return "courses/form";
        }

        try {
            courseService.create(request);
            redirect.addFlashAttribute("success", "Thêm học phần thành công");
            return "redirect:/admin/courses";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("faculties", facultyService.getForPrint());
            model.addAttribute("globalError", e.getReason());
            return "courses/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        CourseResponse c = courseService.getById(id);

        CourseRequest req = new CourseRequest();
        req.setCourseCode(c.getCourseCode());
        req.setCourseName(c.getCourseName());
        req.setCredits(c.getCredits());
        req.setLectureHours(c.getLectureHours());
        req.setPracticeHours(c.getPracticeHours());
        req.setFacultyId(c.getFacultyId());
        req.setDescription(c.getDescription());
        req.setStatus(c.getStatus());

        model.addAttribute("mode", "edit");
        model.addAttribute("courseId", id);
        model.addAttribute("courseRequest", req);
        model.addAttribute("faculties", facultyService.getForPrint());

        return "courses/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("courseRequest") CourseRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("courseId", id);
            model.addAttribute("faculties", facultyService.getForPrint());
            return "courses/form";
        }

        try {
            courseService.update(id, request);
            redirect.addFlashAttribute("success", "Cập nhật học phần thành công");
            return "redirect:/admin/courses";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("courseId", id);
            model.addAttribute("faculties", facultyService.getForPrint());
            model.addAttribute("globalError", e.getReason());
            return "courses/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            courseService.delete(id);
            redirect.addFlashAttribute("success", "Xóa học phần thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không thể xóa học phần");
        }
        return "redirect:/admin/courses";
    }
}
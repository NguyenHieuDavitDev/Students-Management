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

    /* ================= LIST ================= */
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

    /* ================= CREATE FORM ================= */
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("courseRequest", new CourseRequest());
        model.addAttribute("faculties", facultyService.getForPrint());
        return "courses/form";
    }

    /* ================= CREATE ================= */
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

    /* ================= EDIT FORM ================= */
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

    /* ================= UPDATE ================= */
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

    /* ================= DELETE ================= */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {

        try {
            courseService.delete(id);
            redirect.addFlashAttribute("success", "Xóa học phần thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }

        return "redirect:/admin/courses";
    }

    /* ================= EXPORT ================= */
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=courses.xlsx");

            courseService.exportExcel(response);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể export Excel");
        }
    }

    /* ================= IMPORT ================= */
    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {

        if (file == null || file.isEmpty()) {
            redirect.addFlashAttribute("error", "Vui lòng chọn file Excel");
            return "redirect:/admin/courses";
        }

        try {
            int count = courseService.importExcel(file);
            redirect.addFlashAttribute("success", "Đã import " + count + " học phần");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
        }

        return "redirect:/admin/courses";
    }

    /* ================= PRINT ================= */
    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("courses", courseService.getForPrint());
        return "courses/print";
    }
}
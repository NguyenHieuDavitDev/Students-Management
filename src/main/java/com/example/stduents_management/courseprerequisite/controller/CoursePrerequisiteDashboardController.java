package com.example.stduents_management.courseprerequisite.controller;

import com.example.stduents_management.course.dto.CourseResponse;
import com.example.stduents_management.course.service.CourseService;
import com.example.stduents_management.courseprerequisite.dto.CoursePrerequisiteResponse;
import com.example.stduents_management.courseprerequisite.service.CoursePrerequisiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/course-prerequisites")
@RequiredArgsConstructor
public class CoursePrerequisiteDashboardController {

    private final CoursePrerequisiteService coursePrerequisiteService;
    private final CourseService courseService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<CoursePrerequisiteResponse> relations = coursePrerequisiteService.search(keyword, page, size);
        model.addAttribute("relations", relations);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "course-prerequisites/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("allCourses", courseService.getForPrint());
        return "course-prerequisites/form";
    }

    @PostMapping
    public String create(
            @RequestParam("courseId") java.util.UUID courseId,
            @RequestParam("prerequisiteCourseId") java.util.UUID prerequisiteCourseId,
            Model model,
            RedirectAttributes redirect
    ) {
        try {
            coursePrerequisiteService.create(courseId, prerequisiteCourseId);
            redirect.addFlashAttribute("success", "Thêm quan hệ học phần tiên quyết thành công");
            return "redirect:/admin/course-prerequisites";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("allCourses", courseService.getForPrint());
            model.addAttribute("selectedCourseId", courseId);
            model.addAttribute("selectedPrerequisiteCourseId", prerequisiteCourseId);
            model.addAttribute("globalError", e.getReason());
            return "course-prerequisites/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable java.util.UUID id, Model model) {
        CoursePrerequisiteResponse rel = coursePrerequisiteService.getById(id);

        model.addAttribute("mode", "edit");
        model.addAttribute("relationId", id);
        model.addAttribute("allCourses", courseService.getForPrint());
        model.addAttribute("selectedCourseId", rel.getCourseId());
        model.addAttribute("selectedPrerequisiteCourseId", rel.getPrerequisiteCourseId());

        return "course-prerequisites/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable java.util.UUID id,
            @RequestParam("courseId") java.util.UUID courseId,
            @RequestParam("prerequisiteCourseId") java.util.UUID prerequisiteCourseId,
            Model model,
            RedirectAttributes redirect
    ) {
        try {
            coursePrerequisiteService.update(id, courseId, prerequisiteCourseId);
            redirect.addFlashAttribute("success", "Cập nhật học phần tiên quyết thành công");
            return "redirect:/admin/course-prerequisites";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("relationId", id);
            model.addAttribute("allCourses", courseService.getForPrint());
            model.addAttribute("selectedCourseId", courseId);
            model.addAttribute("selectedPrerequisiteCourseId", prerequisiteCourseId);
            model.addAttribute("globalError", e.getReason());
            return "course-prerequisites/form";
        }
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("relations", coursePrerequisiteService.getForPrint());
        return "course-prerequisites/print";
    }
}


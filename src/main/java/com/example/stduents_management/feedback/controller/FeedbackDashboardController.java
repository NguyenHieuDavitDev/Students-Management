package com.example.stduents_management.feedback.controller;

import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.feedback.dto.FeedbackRequest;
import com.example.stduents_management.feedback.dto.FeedbackResponse;
import com.example.stduents_management.feedback.service.FeedbackService;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.student.repository.StudentRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/feedbacks")
@RequiredArgsConstructor
public class FeedbackDashboardController {

    private final FeedbackService feedbackService;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final CourseRepository courseRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Page<FeedbackResponse> items = feedbackService.search(keyword, fromDate, toDate, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("fromDate", fromDate == null ? "" : fromDate);
        model.addAttribute("toDate", toDate == null ? "" : toDate);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "feedbacks/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("feedbackRequest", new FeedbackRequest());
        addFormChoices(model);
        return "feedbacks/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("feedbackRequest") FeedbackRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            addFormChoices(model);
            return "feedbacks/form";
        }
        try {
            feedbackService.create(req);
            redirect.addFlashAttribute("success", "Ghi nhận phản hồi thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getMessage());
            addFormChoices(model);
            return "feedbacks/form";
        }
        return "redirect:/admin/feedbacks";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        FeedbackResponse r = feedbackService.getById(id);
        FeedbackRequest req = new FeedbackRequest();
        req.setStudentId(r.studentId());
        req.setLecturerId(r.lecturerId());
        req.setSubjectId(r.subjectId());
        req.setRating(r.rating());
        req.setComment(r.comment());

        model.addAttribute("mode", "edit");
        model.addAttribute("feedbackId", id);
        model.addAttribute("feedbackRequest", req);
        model.addAttribute("currentItem", r);
        addFormChoices(model);
        return "feedbacks/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("feedbackRequest") FeedbackRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("feedbackId", id);
            addFormChoices(model);
            return "feedbacks/form";
        }
        try {
            feedbackService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật phản hồi thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("feedbackId", id);
            model.addAttribute("globalError", e.getMessage());
            addFormChoices(model);
            return "feedbacks/form";
        }
        return "redirect:/admin/feedbacks";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            feedbackService.delete(id);
            redirect.addFlashAttribute("success", "Xóa phản hồi thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/feedbacks";
    }

    @GetMapping("/print")
    public String print(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Model model
    ) {
        List<FeedbackResponse> items = feedbackService.getAllFiltered(keyword, fromDate, toDate);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("fromDate", fromDate == null ? "" : fromDate);
        model.addAttribute("toDate", toDate == null ? "" : toDate);
        return "feedbacks/print";
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        feedbackService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            int count = feedbackService.importExcel(file);
            redirect.addFlashAttribute("success", "Import thành công " + count + " phản hồi");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
        }
        return "redirect:/admin/feedbacks";
    }

    private void addFormChoices(Model model) {
        model.addAttribute("students", studentRepository.findAll(Sort.by("studentCode")));
        model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
    }
}

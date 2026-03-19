package com.example.stduents_management.graduationresult.controller;

import com.example.stduents_management.graduationresult.dto.GraduationResultRequest;
import com.example.stduents_management.graduationresult.dto.GraduationResultResponse;
import com.example.stduents_management.graduationresult.entity.GraduationResultStatus;
import com.example.stduents_management.graduationresult.service.GraduationResultService;
import com.example.stduents_management.student.dto.StudentResponse;
import com.example.stduents_management.student.service.StudentService;
import com.example.stduents_management.trainingprogram.dto.TrainingProgramResponse;
import com.example.stduents_management.trainingprogram.service.TrainingProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/graduation-results")
@RequiredArgsConstructor
public class GraduationResultDashboardController {

    private final GraduationResultService graduationResultService;
    private final StudentService studentService;
    private final TrainingProgramService trainingProgramService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Page<GraduationResultResponse> items = graduationResultService.search(keyword, status, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("statusFilter", status == null ? "" : status);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("statuses", GraduationResultStatus.values());
        return "graduation-results/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        GraduationResultRequest req = new GraduationResultRequest();
        req.setStatus(GraduationResultStatus.PENDING);
        model.addAttribute("mode", "create");
        model.addAttribute("graduationResultRequest", req);
        model.addAttribute("statuses", GraduationResultStatus.values());
        model.addAttribute("students", getAllStudents());
        model.addAttribute("programs", getAllActivePrograms());
        return "graduation-results/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("graduationResultRequest") GraduationResultRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("statuses", GraduationResultStatus.values());
            model.addAttribute("students", getAllStudents());
            model.addAttribute("programs", getAllActivePrograms());
            return "graduation-results/form";
        }
        try {
            graduationResultService.create(req);
            redirect.addFlashAttribute("success", "Thêm kết quả xét tốt nghiệp thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            model.addAttribute("statuses", GraduationResultStatus.values());
            model.addAttribute("students", getAllStudents());
            model.addAttribute("programs", getAllActivePrograms());
            model.addAttribute("globalError", e.getMessage());
            return "graduation-results/form";
        }
        return "redirect:/admin/graduation-results";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        GraduationResultResponse r = graduationResultService.getById(id);
        GraduationResultRequest req = new GraduationResultRequest();
        req.setStudentId(r.studentId());
        req.setProgramId(r.programId());
        req.setTotalCredits(r.totalCredits());
        req.setGpa(r.gpa());
        req.setCertificates(r.certificates());
        req.setMissingCourses(r.missingCourses());
        req.setNote(r.note());
        req.setStatus(r.status());

        model.addAttribute("mode", "edit");
        model.addAttribute("resultId", id);
        model.addAttribute("graduationResultRequest", req);
        model.addAttribute("statuses", GraduationResultStatus.values());
        model.addAttribute("students", getAllStudents());
        model.addAttribute("programs", getAllActivePrograms());
        model.addAttribute("currentResult", r);
        return "graduation-results/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("graduationResultRequest") GraduationResultRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("resultId", id);
            model.addAttribute("statuses", GraduationResultStatus.values());
            model.addAttribute("students", getAllStudents());
            model.addAttribute("programs", getAllActivePrograms());
            return "graduation-results/form";
        }
        try {
            graduationResultService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật kết quả xét tốt nghiệp thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("resultId", id);
            model.addAttribute("statuses", GraduationResultStatus.values());
            model.addAttribute("students", getAllStudents());
            model.addAttribute("programs", getAllActivePrograms());
            model.addAttribute("globalError", e.getMessage());
            return "graduation-results/form";
        }
        return "redirect:/admin/graduation-results";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            graduationResultService.delete(id);
            redirect.addFlashAttribute("success", "Xóa kết quả xét tốt nghiệp thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/graduation-results";
    }

    private List<StudentResponse> getAllStudents() {
        return studentService.search(null, 0, Integer.MAX_VALUE).getContent();
    }

    private List<TrainingProgramResponse> getAllActivePrograms() {
        return trainingProgramService.search(null, 0, Integer.MAX_VALUE)
                .getContent()
                .stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsActive()))
                .toList();
    }
}


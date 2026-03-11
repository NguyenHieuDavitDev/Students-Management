package com.example.stduents_management.gradescale.controller;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.gradecomponent.entity.GradeComponent;
import com.example.stduents_management.gradescale.dto.GradeScaleRequest;
import com.example.stduents_management.gradescale.dto.GradeScaleResponse;
import com.example.stduents_management.gradescale.dto.StudentTranscriptResult;
import com.example.stduents_management.gradescale.service.GradeScaleService;
import com.example.stduents_management.gradescale.service.GradeTranscriptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/grade-scales")
@RequiredArgsConstructor
public class GradeScaleDashboardController {

    private final GradeScaleService gradeScaleService;
    private final GradeTranscriptService gradeTranscriptService;

    // ─── INDEX: Quản lý thang điểm ───────────────────────────────────────────

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Page<GradeScaleResponse> items = gradeScaleService.search(keyword, page, size);
        model.addAttribute("items", items);
        model.addAttribute("allItems", gradeScaleService.getAll());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "grade-scales/index";
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("gradeScaleRequest", new GradeScaleRequest());
        model.addAttribute("allItems", gradeScaleService.getAll());
        return "grade-scales/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("gradeScaleRequest") GradeScaleRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("allItems", gradeScaleService.getAll());
            return "grade-scales/form";
        }
        try {
            gradeScaleService.create(req);
            redirect.addFlashAttribute("success", "Thêm thang điểm thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            model.addAttribute("allItems", gradeScaleService.getAll());
            model.addAttribute("globalError", e.getMessage());
            return "grade-scales/form";
        }
        return "redirect:/admin/grade-scales";
    }

    // ─── EDIT ────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        GradeScaleResponse r = gradeScaleService.getById(id);
        GradeScaleRequest req = new GradeScaleRequest();
        req.setMinScore(r.minScore());
        req.setMaxScore(r.maxScore());
        req.setLetterGrade(r.letterGrade());
        req.setGradePoint(r.gradePoint());
        req.setDescription(r.description());
        model.addAttribute("mode", "edit");
        model.addAttribute("scaleId", id);
        model.addAttribute("gradeScaleRequest", req);
        model.addAttribute("allItems", gradeScaleService.getAll());
        return "grade-scales/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("gradeScaleRequest") GradeScaleRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("scaleId", id);
            model.addAttribute("allItems", gradeScaleService.getAll());
            return "grade-scales/form";
        }
        try {
            gradeScaleService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật thang điểm thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("scaleId", id);
            model.addAttribute("allItems", gradeScaleService.getAll());
            model.addAttribute("globalError", e.getMessage());
            return "grade-scales/form";
        }
        return "redirect:/admin/grade-scales";
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            gradeScaleService.delete(id);
            redirect.addFlashAttribute("success", "Xóa thang điểm thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/grade-scales";
    }

    // ─── PRINT ────────────────────────────────────────────────────────────────

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("items", gradeScaleService.getAll());
        return "grade-scales/print";
    }

    // ─── BẢNG ĐIỂM TỔNG KẾT (Bước 3 + Bước 4) ───────────────────────────────

    /**
     * Trang xem bảng điểm tổng kết theo lớp học phần.
     * Khi không chọn lớp: hiển thị selector.
     * Khi chọn lớp: tính toán và hiển thị kết quả.
     */
    @GetMapping("/transcript")
    public String transcript(
            @RequestParam(required = false) Long courseClassId,
            Model model) {

        List<ClassSection> allClasses = gradeTranscriptService.getAllClasses();
        model.addAttribute("allClasses", allClasses);
        model.addAttribute("selectedClassId", courseClassId);

        if (courseClassId != null) {
            try {
                List<StudentTranscriptResult> transcripts =
                        gradeTranscriptService.calculateForClass(courseClassId);
                List<GradeComponent> components =
                        gradeTranscriptService.getComponents(courseClassId);

                // Lấy thông tin lớp từ kết quả đầu tiên (nếu có)
                String classCode = "";
                String className = "";
                String courseName = "";
                if (!transcripts.isEmpty()) {
                    StudentTranscriptResult first = transcripts.get(0);
                    classCode  = first.classCode()  != null ? first.classCode()  : "";
                    className  = first.className()  != null ? first.className()  : "";
                    courseName = first.courseName() != null ? first.courseName() : "";
                } else {
                    // Lớp có đăng ký nhưng chưa có sinh viên hoặc class info từ DB
                    allClasses.stream()
                              .filter(c -> c.getId().equals(courseClassId))
                              .findFirst()
                              .ifPresent(c -> {
                                  model.addAttribute("classCode",  c.getClassCode());
                                  model.addAttribute("className",  c.getClassName());
                                  model.addAttribute("courseName",
                                          c.getCourse() != null ? c.getCourse().getCourseName() : "");
                              });
                }

                model.addAttribute("transcripts", transcripts);
                model.addAttribute("components", components);
                model.addAttribute("classCode",  classCode);
                model.addAttribute("className",  className);
                model.addAttribute("courseName", courseName);

                // Thống kê nhanh
                long passed = transcripts.stream()
                        .filter(t -> t.totalScore10() != null
                                && t.totalScore10().compareTo(new java.math.BigDecimal("5.0")) >= 0)
                        .count();
                model.addAttribute("totalStudents", transcripts.size());
                model.addAttribute("passedCount",   passed);
                model.addAttribute("failedCount",   transcripts.size() - passed);

            } catch (Exception e) {
                model.addAttribute("transcriptError", e.getMessage());
                model.addAttribute("transcripts", Collections.emptyList());
                model.addAttribute("components",  Collections.emptyList());
            }
        }

        return "grade-scales/transcript";
    }

    /**
     * In bảng điểm tổng kết theo lớp.
     */
    @GetMapping("/transcript/print")
    public String printTranscript(
            @RequestParam Long courseClassId,
            Model model) {
        List<StudentTranscriptResult> transcripts =
                gradeTranscriptService.calculateForClass(courseClassId);
        List<GradeComponent> components =
                gradeTranscriptService.getComponents(courseClassId);

        model.addAttribute("transcripts", transcripts);
        model.addAttribute("components", components);

        if (!transcripts.isEmpty()) {
            StudentTranscriptResult first = transcripts.get(0);
            model.addAttribute("classCode",  first.classCode());
            model.addAttribute("className",  first.className());
            model.addAttribute("courseName", first.courseName());
        }
        return "grade-scales/transcript-print";
    }
}

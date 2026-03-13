package com.example.stduents_management.studenttuition.controller;

import com.example.stduents_management.semester.entity.Semester;
import com.example.stduents_management.semester.repository.SemesterRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.student.repository.StudentRepository;
import com.example.stduents_management.studenttuition.dto.StudentTuitionRequest;
import com.example.stduents_management.studenttuition.dto.StudentTuitionResponse;
import com.example.stduents_management.studenttuition.entity.StudentTuitionStatus;
import com.example.stduents_management.studenttuition.service.StudentTuitionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/student-tuition")
@RequiredArgsConstructor
public class StudentTuitionDashboardController {

    private final StudentTuitionService studentTuitionService;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;

    // ─── INDEX ────────────────────────────────────────────────────────────────

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Page<StudentTuitionResponse> items = studentTuitionService.search(keyword, status, page, size);
        long unpaidCount   = items.getContent().stream()
                .filter(i -> i.status() == StudentTuitionStatus.UNPAID).count();
        long partialCount  = items.getContent().stream()
                .filter(i -> i.status() == StudentTuitionStatus.PARTIAL).count();
        long paidCount     = items.getContent().stream()
                .filter(i -> i.status() == StudentTuitionStatus.PAID).count();

        model.addAttribute("items", items);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("partialCount", partialCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("statusFilter", status == null ? "" : status);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("statuses", StudentTuitionStatus.values());
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("semesters", semesterRepository.findAll());
        return "student-tuition/index";
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("studentTuitionRequest", new StudentTuitionRequest());
        loadSelectData(model);
        return "student-tuition/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("studentTuitionRequest") StudentTuitionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            return "student-tuition/form";
        }
        try {
            studentTuitionService.create(req);
            redirect.addFlashAttribute("success", "Thêm học phí sinh viên thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            model.addAttribute("globalError", e.getMessage());
            return "student-tuition/form";
        }
        return "redirect:/admin/student-tuition";
    }

    // ─── EDIT ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        StudentTuitionResponse r = studentTuitionService.getById(id);

        StudentTuitionRequest req = new StudentTuitionRequest();
        req.setStudentId(r.studentId());
        req.setSemesterId(r.semesterId());
        req.setTotalCredits(r.totalCredits());
        req.setTotalAmount(r.totalAmount());
        req.setAmountPaid(r.amountPaid());
        req.setStatus(r.status());

        model.addAttribute("mode", "edit");
        model.addAttribute("tuitionId", id);
        model.addAttribute("studentTuitionRequest", req);
        loadSelectData(model);
        model.addAttribute("currentItem", r);
        return "student-tuition/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("studentTuitionRequest") StudentTuitionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("tuitionId", id);
            loadSelectData(model);
            return "student-tuition/form";
        }
        try {
            studentTuitionService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật học phí sinh viên thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("tuitionId", id);
            loadSelectData(model);
            model.addAttribute("globalError", e.getMessage());
            return "student-tuition/form";
        }
        return "redirect:/admin/student-tuition";
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            studentTuitionService.delete(id);
            redirect.addFlashAttribute("success", "Xóa học phí sinh viên thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/student-tuition";
    }

    // ─── PRINT ────────────────────────────────────────────────────────────────

    @GetMapping("/print")
    public String print(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model) {
        List<StudentTuitionResponse> all = studentTuitionService.getAll();
        long unpaidCount   = all.stream().filter(i -> i.status() == StudentTuitionStatus.UNPAID).count();
        long partialCount  = all.stream().filter(i -> i.status() == StudentTuitionStatus.PARTIAL).count();
        long paidCount     = all.stream().filter(i -> i.status() == StudentTuitionStatus.PAID).count();
        model.addAttribute("items", all);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("partialCount", partialCount);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("statusFilter", status == null ? "" : status);
        return "student-tuition/print";
    }

    // ─── EXPORT EXCEL ─────────────────────────────────────────────────────────

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        studentTuitionService.exportExcel(response);
    }

    // ─── IMPORT EXCEL ─────────────────────────────────────────────────────────

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect) {
        try {
            int count = studentTuitionService.importExcel(file);
            redirect.addFlashAttribute("success",
                    "Import thành công " + count + " bản ghi học phí sinh viên");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
        }
        return "redirect:/admin/student-tuition";
    }

    // ─── API: TÍNH HỌC PHÍ TỰ ĐỘNG ─────────────────────────────────────────────

    /**
     * API tính toán tổng học phí dựa trên sinh viên, học kỳ và tổng số tín chỉ.
     * Dùng cho form Thymeleaf khi người dùng nhập số tín chỉ để tự động gợi ý tổng học phí.
     */
    @GetMapping("/api/calc-total-amount")
    @ResponseBody
    public ResponseEntity<?> calcTotalAmount(
            @RequestParam UUID studentId,
            @RequestParam(required = false) Long semesterId,
            @RequestParam Integer totalCredits
    ) {
        try {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));
            Semester semester = null;
            if (semesterId != null) {
                semester = semesterRepository.findById(semesterId)
                        .orElse(null);
            }
            var amount = studentTuitionService.calculateTuitionAmountForStudent(student, semester, totalCredits);
            return ResponseEntity.ok().body(amount);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("0");
        }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void loadSelectData(Model model) {
        List<Student> students = studentRepository.findAll();
        List<Semester> semesters = semesterRepository.findAll();
        model.addAttribute("students", students);
        model.addAttribute("semesters", semesters);
        model.addAttribute("statuses", StudentTuitionStatus.values());
    }
}


package com.example.stduents_management.studentgrade.controller;

import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.courseregistration.repository.CourseRegistrationRepository;
import com.example.stduents_management.gradecomponent.repository.GradeComponentRepository;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.studentgrade.dto.StudentGradeRequest;
import com.example.stduents_management.studentgrade.dto.StudentGradeResponse;
import com.example.stduents_management.studentgrade.service.StudentGradeService;
import com.example.stduents_management.user.service.CurrentUserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/student-grades")
@RequiredArgsConstructor
public class StudentGradeDashboardController {

    private final StudentGradeService studentGradeService;
    private final ClassSectionRepository classSectionRepository;
    private final GradeComponentRepository gradeComponentRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final LecturerRepository lecturerRepository;
    private final CurrentUserProfileService currentUserProfileService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long courseClassId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<StudentGradeResponse> items = studentGradeService.search(keyword, courseClassId, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("courseClassId", courseClassId);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        loadSelectData(model);
        return "student-grades/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        StudentGradeRequest req = new StudentGradeRequest();
        currentUserProfileService.getCurrentLecturerId().ifPresent(req::setGradedByLecturerId);
        model.addAttribute("mode", "create");
        model.addAttribute("studentGradeRequest", req);
        loadSelectData(model);
        return "student-grades/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("studentGradeRequest") StudentGradeRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            return "student-grades/form";
        }
        Optional<UUID> lecturerId = req.getGradedByLecturerId() != null
                ? Optional.of(req.getGradedByLecturerId())
                : currentUserProfileService.getCurrentLecturerId();
        studentGradeService.save(req, lecturerId);
        redirect.addFlashAttribute("success", "Nhập điểm thành công");
        return "redirect:/admin/student-grades";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        StudentGradeResponse r = studentGradeService.getById(id);
        StudentGradeRequest req = new StudentGradeRequest();
        req.setStudentId(r.studentId());
        req.setCourseClassId(r.courseClassId());
        req.setGradeComponentId(r.gradeComponentId());
        req.setScore(r.score());
        req.setGradedByLecturerId(r.gradedByLecturerId());
        model.addAttribute("mode", "edit");
        model.addAttribute("gradeId", id);
        model.addAttribute("studentGradeRequest", req);
        loadSelectData(model);
        if (r.courseClassId() != null) {
            model.addAttribute("editModeStudents",
                    courseRegistrationRepository.findByClassSection_IdOrderByStudent_FullName(r.courseClassId()));
            model.addAttribute("editModeGradeComponents",
                    gradeComponentRepository.findByClassSection_IdOrderByComponentName(r.courseClassId()));
        }
        return "student-grades/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("studentGradeRequest") StudentGradeRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("gradeId", id);
            loadSelectData(model);
            if (req.getCourseClassId() != null) {
                model.addAttribute("editModeStudents",
                        courseRegistrationRepository.findByClassSection_IdOrderByStudent_FullName(req.getCourseClassId()));
                model.addAttribute("editModeGradeComponents",
                        gradeComponentRepository.findByClassSection_IdOrderByComponentName(req.getCourseClassId()));
            }
            return "student-grades/form";
        }
        Optional<UUID> lecturerId = req.getGradedByLecturerId() != null
                ? Optional.of(req.getGradedByLecturerId())
                : currentUserProfileService.getCurrentLecturerId();
        studentGradeService.save(req, lecturerId);
        redirect.addFlashAttribute("success", "Cập nhật điểm thành công");
        return "redirect:/admin/student-grades";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        studentGradeService.delete(id);
        redirect.addFlashAttribute("success", "Xóa điểm thành công");
        return "redirect:/admin/student-grades";
    }

    @GetMapping("/print")
    public String print(
            @RequestParam(required = false) Long courseClassId,
            Model model) {
        List<StudentGradeResponse> items = courseClassId != null
                ? studentGradeService.getForPrintByCourseClass(courseClassId)
                : studentGradeService.getForPrint();
        model.addAttribute("items", items);
        model.addAttribute("courseClassId", courseClassId);
        loadSelectData(model);
        return "student-grades/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect) {
        try {
            studentGradeService.importExcel(file, currentUserProfileService.getCurrentLecturerId());
            redirect.addFlashAttribute("success", "Import điểm thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/student-grades";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel(
            @RequestParam(required = false) Long courseClassId) throws Exception {
        byte[] data = studentGradeService.exportExcel(courseClassId);
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-grades.xlsx");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/template")
    public ResponseEntity<ByteArrayResource> downloadTemplate() throws Exception {
        byte[] data = studentGradeService.getImportTemplate();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-grades-template.xlsx");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/api/students-by-class")
    @ResponseBody
    public List<Map<String, Object>> studentsByClass(@RequestParam Long courseClassId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var cr : courseRegistrationRepository.findByClassSection_IdOrderByStudent_FullName(courseClassId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", cr.getStudent().getStudentId().toString());
            m.put("studentCode", cr.getStudent().getStudentCode());
            m.put("fullName", cr.getStudent().getFullName());
            result.add(m);
        }
        return result;
    }

    @GetMapping("/api/grade-components-by-class")
    @ResponseBody
    public List<Map<String, Object>> gradeComponentsByClass(@RequestParam Long courseClassId) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var gc : gradeComponentRepository.findByClassSection_IdOrderByComponentName(courseClassId)) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", gc.getId().toString());
            m.put("componentName", gc.getComponentName());
            m.put("maxScore", gc.getMaxScore() != null ? gc.getMaxScore() : java.math.BigDecimal.valueOf(10));
            result.add(m);
        }
        return result;
    }

    private void loadSelectData(Model model) {
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        model.addAttribute("gradeComponents", gradeComponentRepository.findAll(Sort.by("componentName")));
        model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("fullName")));
    }
}

package com.example.stduents_management.attendance.controller;

import com.example.stduents_management.attendance.dto.AttendanceRequest;
import com.example.stduents_management.attendance.dto.AttendanceResponse;
import com.example.stduents_management.attendance.service.AttendanceService;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.courseregistration.entity.CourseRegistration;
import com.example.stduents_management.courseregistration.repository.CourseRegistrationRepository;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturercourseclass.repository.LecturerCourseClassRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.student.repository.StudentRepository;
import com.example.stduents_management.user.service.CurrentUserProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/admin/attendances")
@RequiredArgsConstructor
public class AttendanceDashboardController {

    private final AttendanceService attendanceService;
    private final CurrentUserProfileService currentUserProfileService;

    private final ClassSectionRepository classSectionRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final LecturerCourseClassRepository lecturerCourseClassRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long courseClassId,
            @RequestParam(required = false) String attendanceDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        LocalDate date = AttendanceService.parseDate(attendanceDate);
        Page<AttendanceResponse> items = attendanceService.search(keyword, courseClassId, date, page, size);

        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("courseClassId", courseClassId);
        model.addAttribute("attendanceDate", attendanceDate == null ? "" : attendanceDate);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        return "attendances/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        AttendanceRequest req = new AttendanceRequest();
        req.setAttendanceDate(LocalDate.now());
        req.setPresent(false);
        currentUserProfileService.getCurrentLecturerId().ifPresent(req::setMarkedByLecturerId);

        model.addAttribute("mode", "create");
        model.addAttribute("attendanceRequest", req);
        model.addAttribute("attendanceId", null);
        model.addAttribute("students", Collections.emptyList());
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        model.addAttribute("lecturers", Collections.emptyList());
        return "attendances/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("attendanceRequest") AttendanceRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("students", Collections.emptyList());
            model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
            if (req.getCourseClassId() != null) {
                model.addAttribute("lecturers",
                        lecturerCourseClassRepository.findLecturersByClassSectionId(req.getCourseClassId()));
            } else {
                model.addAttribute("lecturers", Collections.emptyList());
            }
            return "attendances/form";
        }

        Optional<UUID> lecturerId = req.getMarkedByLecturerId() != null
                ? Optional.of(req.getMarkedByLecturerId())
                : currentUserProfileService.getCurrentLecturerId();

        AttendanceResponse saved = attendanceService.upsert(null, req, lecturerId);

        redirect.addFlashAttribute("success", "Ghi nhận điểm danh thành công");
        if (saved != null && saved.courseClassId() != null && saved.attendanceDate() != null) {
            return "redirect:/admin/attendances?courseClassId=" + saved.courseClassId()
                    + "&attendanceDate=" + saved.attendanceDate();
        }
        return "redirect:/admin/attendances";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        AttendanceResponse r = attendanceService.getById(id);
        AttendanceRequest req = new AttendanceRequest();
        req.setStudentId(r.studentId());
        req.setCourseClassId(r.courseClassId());
        req.setAttendanceDate(r.attendanceDate());
        req.setPresent(r.present());
        req.setNote(r.note());
        req.setMarkedByLecturerId(r.lecturerId());

        Student student = studentRepository.findById(r.studentId()).orElse(null);
        List<Student> students = student != null ? List.of(student) : Collections.emptyList();

        List<Lecturer> eligibleLecturers = lecturerCourseClassRepository.findLecturersByClassSectionId(r.courseClassId());
        if (req.getMarkedByLecturerId() == null && !eligibleLecturers.isEmpty()) {
            req.setMarkedByLecturerId(eligibleLecturers.get(0).getLecturerId());
        }

        model.addAttribute("mode", "edit");
        model.addAttribute("attendanceId", id);
        model.addAttribute("attendanceRequest", req);
        model.addAttribute("students", students);
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        model.addAttribute("lecturers", eligibleLecturers);
        model.addAttribute("currentItem", r);
        return "attendances/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("attendanceRequest") AttendanceRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("attendanceId", id);
            Student student = null;
            if (req.getStudentId() != null) {
                student = studentRepository.findById(req.getStudentId()).orElse(null);
            }
            model.addAttribute("students", student != null ? List.of(student) : Collections.emptyList());
            model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
            if (req.getCourseClassId() != null) {
                model.addAttribute("lecturers",
                        lecturerCourseClassRepository.findLecturersByClassSectionId(req.getCourseClassId()));
            } else {
                model.addAttribute("lecturers", Collections.emptyList());
            }
            return "attendances/form";
        }

        Optional<UUID> lecturerId = req.getMarkedByLecturerId() != null
                ? Optional.of(req.getMarkedByLecturerId())
                : currentUserProfileService.getCurrentLecturerId();

        AttendanceResponse saved = attendanceService.upsert(id, req, lecturerId);
        redirect.addFlashAttribute("success", "Cập nhật điểm danh thành công");
        if (saved != null && saved.courseClassId() != null && saved.attendanceDate() != null) {
            return "redirect:/admin/attendances?courseClassId=" + saved.courseClassId()
                    + "&attendanceDate=" + saved.attendanceDate();
        }
        return "redirect:/admin/attendances";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            attendanceService.delete(id);
            redirect.addFlashAttribute("success", "Xóa bản ghi điểm danh thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/attendances";
    }

    @PostMapping("/initialize")
    public String initialize(
            @RequestParam Long courseClassId,
            @RequestParam String attendanceDate,
            @RequestParam(required = false) UUID markedByLecturerId,
            RedirectAttributes redirect
    ) {
        try {
            LocalDate date = AttendanceService.parseDate(attendanceDate);
            Optional<UUID> lecturerId = markedByLecturerId != null
                    ? Optional.of(markedByLecturerId)
                    : currentUserProfileService.getCurrentLecturerId();
            attendanceService.initializeForClassAndDate(courseClassId, date, lecturerId);
            redirect.addFlashAttribute("success", "Đã tạo bản ghi điểm danh cho lớp và ngày này");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/attendances?courseClassId=" + courseClassId + "&attendanceDate=" + attendanceDate;
    }

    @GetMapping("/export")
    public void exportExcel(
            HttpServletResponse response,
            @RequestParam Long courseClassId,
            @RequestParam String attendanceDate
    ) {
        LocalDate date = AttendanceService.parseDate(attendanceDate);
        attendanceService.exportExcel(response, courseClassId, date);
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam MultipartFile file,
            @RequestParam Long courseClassId,
            @RequestParam String attendanceDate,
            @RequestParam(required = false) UUID markedByLecturerId,
            RedirectAttributes redirect
    ) {
        try {
            LocalDate date = AttendanceService.parseDate(attendanceDate);
            Optional<UUID> lecturerId = markedByLecturerId != null
                    ? Optional.of(markedByLecturerId)
                    : currentUserProfileService.getCurrentLecturerId();

            int count = attendanceService.importExcel(file, courseClassId, date, lecturerId);
            redirect.addFlashAttribute("success", "Import thành công " + count + " bản ghi");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
        }
        return "redirect:/admin/attendances?courseClassId=" + courseClassId + "&attendanceDate=" + attendanceDate;
    }

    @GetMapping("/template")
    public ResponseEntity<Resource> downloadTemplate() {
        byte[] bytes = attendanceService.getImportTemplate();
        ByteArrayResource resource = new ByteArrayResource(bytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendances-template.xlsx");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/print")
    public String print(
            @RequestParam Long courseClassId,
            @RequestParam String attendanceDate,
            Model model
    ) {
        LocalDate date = AttendanceService.parseDate(attendanceDate);
        List<AttendanceResponse> items = attendanceService.getForPrint(courseClassId, date);
        model.addAttribute("items", items);
        model.addAttribute("courseClassId", courseClassId);
        model.addAttribute("attendanceDate", attendanceDate);
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        return "attendances/print";
    }

    @GetMapping("/api/students-by-class")
    @ResponseBody
    public List<Map<String, Object>> studentsByClass(@RequestParam Long courseClassId) {
        List<CourseRegistration> regs = courseRegistrationRepository.findByClassSection_IdOrderByStudent_FullName(courseClassId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (CourseRegistration cr : regs) {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", cr.getStudent().getStudentId().toString());
            m.put("studentCode", cr.getStudent().getStudentCode());
            m.put("fullName", cr.getStudent().getFullName());
            result.add(m);
        }
        return result;
    }

    @GetMapping("/api/lecturers-by-class")
    @ResponseBody
    public List<Map<String, Object>> lecturersByClass(@RequestParam Long courseClassId) {
        List<Lecturer> lecturers = lecturerCourseClassRepository.findLecturersByClassSectionId(courseClassId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Lecturer lec : lecturers) {
            Map<String, Object> m = new HashMap<>();
            m.put("lecturerId", lec.getLecturerId().toString());
            m.put("lecturerCode", lec.getLecturerCode());
            m.put("fullName", lec.getFullName());
            result.add(m);
        }
        return result;
    }
}


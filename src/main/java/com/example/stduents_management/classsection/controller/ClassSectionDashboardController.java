package com.example.stduents_management.classsection.controller;

import com.example.stduents_management.classroom.repository.ClassRepository;
import com.example.stduents_management.classsection.dto.ClassSectionRequest;
import com.example.stduents_management.classsection.dto.ClassSectionResponse;
import com.example.stduents_management.classsection.entity.ClassSectionStatus;
import com.example.stduents_management.classsection.service.ClassSectionService;
import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.semester.repository.SemesterRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/class-sections")
@RequiredArgsConstructor
public class ClassSectionDashboardController {

    private final ClassSectionService service;
    private final ClassRepository classRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final RoomRepository roomRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<ClassSectionResponse> sections = service.search(keyword, page, size);
        model.addAttribute("sections", sections);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "class-sections/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        ClassSectionRequest req = new ClassSectionRequest();
        req.setStatus(ClassSectionStatus.OPEN);
        req.setCurrentStudents(0);
        model.addAttribute("mode", "create");
        model.addAttribute("sectionRequest", req);
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
        model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
        model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
        model.addAttribute("administrativeClasses", classRepository.findAll(Sort.by("classCode")));
        model.addAttribute("statuses", ClassSectionStatus.values());
        return "class-sections/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("sectionRequest") ClassSectionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
            model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
            model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
            model.addAttribute("administrativeClasses", classRepository.findAll(Sort.by("classCode")));
            model.addAttribute("statuses", ClassSectionStatus.values());
            return "class-sections/form";
        }
        try {
            service.create(req);
            redirect.addFlashAttribute("success", "Thêm lớp học phần thành công");
        } catch (ResponseStatusException e) {
            model.addAttribute("error", e.getReason());
            model.addAttribute("mode", "create");
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
            model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
            model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
            model.addAttribute("administrativeClasses", classRepository.findAll(Sort.by("classCode")));
            model.addAttribute("statuses", ClassSectionStatus.values());
            return "class-sections/form";
        }
        return "redirect:/admin/class-sections";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        ClassSectionResponse r = service.getById(id);
        ClassSectionRequest req = new ClassSectionRequest();
        req.setCourseId(r.courseId());
        req.setSemesterId(r.semesterId());
        req.setClassCode(r.classCode());
        req.setClassName(r.className());
        req.setMaxStudents(r.maxStudents());
        req.setCurrentStudents(r.currentStudents());
        req.setStatus(r.status());
        req.setRoomId(r.roomId());
        req.setAdministrativeClassId(r.administrativeClassId());
        req.setNote(r.note());
        model.addAttribute("mode", "edit");
        model.addAttribute("sectionId", id);
        model.addAttribute("sectionRequest", req);
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
        model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
        model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
        model.addAttribute("administrativeClasses", classRepository.findAll(Sort.by("classCode")));
        model.addAttribute("statuses", ClassSectionStatus.values());
        return "class-sections/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("sectionRequest") ClassSectionRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("sectionId", id);
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
            model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
            model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
            model.addAttribute("administrativeClasses", classRepository.findAll(Sort.by("classCode")));
            model.addAttribute("statuses", ClassSectionStatus.values());
            return "class-sections/form";
        }
        try {
            service.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật lớp học phần thành công");
        } catch (ResponseStatusException e) {
            model.addAttribute("error", e.getReason());
            model.addAttribute("mode", "edit");
            model.addAttribute("sectionId", id);
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
            model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
            model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
            model.addAttribute("administrativeClasses", classRepository.findAll(Sort.by("classCode")));
            model.addAttribute("statuses", ClassSectionStatus.values());
            return "class-sections/form";
        }
        return "redirect:/admin/class-sections";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("success", "Xóa lớp học phần thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/class-sections";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("sections", service.getForPrint());
        return "class-sections/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            service.importExcel(file);
            redirect.addFlashAttribute("success", "Import lớp học phần thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/class-sections";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = service.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=class_sections.xlsx");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}

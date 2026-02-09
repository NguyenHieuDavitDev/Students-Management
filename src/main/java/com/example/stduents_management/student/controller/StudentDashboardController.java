package com.example.stduents_management.student.controller;


import com.example.stduents_management.classroom.repository.ClassRepository;
import com.example.stduents_management.student.dto.StudentRequest;
import com.example.stduents_management.student.dto.StudentResponse;
import com.example.stduents_management.student.service.StudentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/students")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentService studentService;
    private final ClassRepository classRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        model.addAttribute("students",
                studentService.search(keyword, page, size));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "students/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("studentRequest", new StudentRequest());
        model.addAttribute("classes", classRepository.findAll());
        return "students/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute StudentRequest req,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("classes", classRepository.findAll());
            return "students/form";
        }
        studentService.create(req);
        return "redirect:/admin/students";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        StudentResponse s = studentService.getById(id);

        StudentRequest req = new StudentRequest();
        req.setStudentCode(s.getStudentCode());
        req.setFullName(s.getFullName());
        req.setDateOfBirth(s.getDateOfBirth());
        req.setGender(s.getGender());
        req.setCitizenId(s.getCitizenId());
        req.setEmail(s.getEmail());
        req.setPhoneNumber(s.getPhoneNumber());
        req.setAddress(s.getAddress());
        req.setClassId(s.getClassId());

        model.addAttribute("mode", "edit");
        model.addAttribute("studentId", id);
        model.addAttribute("studentRequest", req);
        model.addAttribute("classes", classRepository.findAll());
        return "students/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute StudentRequest req,
            BindingResult result,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("classes", classRepository.findAll());
            return "students/form";
        }
        studentService.update(id, req);
        return "redirect:/admin/students";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        studentService.delete(id);
        return "redirect:/admin/students";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("students", studentService.getForPrint());
        return "students/print";
    }
}


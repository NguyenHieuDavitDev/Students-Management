package com.example.stduents_management.employee.controller;

import com.example.stduents_management.department.repository.DepartmentRepository;
import com.example.stduents_management.faculty.repository.FacultyRepository;
import com.example.stduents_management.employee.dto.EmployeeRequest;
import com.example.stduents_management.employee.dto.EmployeeResponse;
import com.example.stduents_management.employee.entity.EmployeeType;
import com.example.stduents_management.employee.service.EmployeeService;
import com.example.stduents_management.position.repository.PositionRepository;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
public class EmployeeDashboardController {

    private final EmployeeService employeeService;
    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmployeeType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<EmployeeResponse> employees = employeeService.search(keyword, type, page, size);
        model.addAttribute("employees", employees);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("type", type);
        model.addAttribute("types", EmployeeType.values());
        return "employees/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("employeeRequest", new EmployeeRequest());
        model.addAttribute("types", EmployeeType.values());
        model.addAttribute("positions", positionRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("faculties", facultyRepository.findAll());
        return "employees/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute EmployeeRequest employeeRequest,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("types", EmployeeType.values());
            model.addAttribute("positions", positionRepository.findAll());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("faculties", facultyRepository.findAll());
            return "employees/form";
        }

        try {
            employeeService.create(employeeRequest);
            redirect.addFlashAttribute("success", "Thêm nhân sự thành công");
            return "redirect:/admin/employees";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getReason());
            model.addAttribute("types", EmployeeType.values());
            model.addAttribute("positions", positionRepository.findAll());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("faculties", facultyRepository.findAll());
            return "employees/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        EmployeeResponse e = employeeService.getById(id);
        EmployeeRequest req = new EmployeeRequest();
        req.setEmployeeCode(e.employeeCode());
        req.setFullName(e.fullName());
        req.setDateOfBirth(e.dateOfBirth());
        req.setGender(e.gender());
        req.setCitizenId(e.citizenId());
        req.setEmail(e.email());
        req.setPhoneNumber(e.phoneNumber());
        req.setAddress(e.address());
        req.setAvatar(e.avatar());
        req.setEmployeeType(e.employeeType());
        req.setStatus(e.status());
        req.setPositionId(e.positionId());
        req.setDepartmentId(e.departmentId());
        req.setFacultyId(e.lecturerFacultyId());

        model.addAttribute("mode", "edit");
        model.addAttribute("employeeId", id);
        model.addAttribute("employeeRequest", req);
        model.addAttribute("types", EmployeeType.values());
        model.addAttribute("positions", positionRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("faculties", facultyRepository.findAll());
        return "employees/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute EmployeeRequest employeeRequest,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("employeeId", id);
            model.addAttribute("types", EmployeeType.values());
            model.addAttribute("positions", positionRepository.findAll());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("faculties", facultyRepository.findAll());
            return "employees/form";
        }

        try {
            employeeService.update(id, employeeRequest);
            redirect.addFlashAttribute("success", "Cập nhật nhân sự thành công");
            return "redirect:/admin/employees";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("employeeId", id);
            model.addAttribute("globalError", e.getReason());
            model.addAttribute("types", EmployeeType.values());
            model.addAttribute("positions", positionRepository.findAll());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("faculties", facultyRepository.findAll());
            return "employees/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            employeeService.delete(id);
            redirect.addFlashAttribute("success", "Xóa nhân sự thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        employeeService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam MultipartFile file, RedirectAttributes redirect) {
        try {
            int count = employeeService.importExcel(file);
            redirect.addFlashAttribute("success", "Đã import " + count + " nhân sự");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/employees";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("employees", employeeService.getForPrint());
        return "employees/print";
    }
}


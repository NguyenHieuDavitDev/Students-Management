package com.example.stduents_management.employee.controller;

import com.example.stduents_management.employee.dto.EmployeeRequest;
import com.example.stduents_management.employee.dto.EmployeeResponse;
import com.example.stduents_management.employee.entity.EmployeeType;
import com.example.stduents_management.employee.service.EmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EmployeeType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(employeeService.search(keyword, type, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(@PathVariable UUID id, @Valid @RequestBody EmployeeRequest req) {
        return ResponseEntity.ok(employeeService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        employeeService.exportExcel(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importExcel(@RequestParam MultipartFile file) {
        int count = employeeService.importExcel(file);
        return ResponseEntity.ok("Đã import " + count + " nhân sự");
    }

    @GetMapping("/print")
    public ResponseEntity<List<EmployeeResponse>> print() {
        return ResponseEntity.ok(employeeService.getForPrint());
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmployeeResponse>> all() {
        return ResponseEntity.ok(employeeService.getForPrint());
    }
}


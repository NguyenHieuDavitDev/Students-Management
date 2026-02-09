package com.example.stduents_management.student.controller;

import com.example.stduents_management.student.dto.StudentRequest;
import com.example.stduents_management.student.dto.StudentResponse;
import com.example.stduents_management.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /* ===================== GET LIST ===================== */
    @GetMapping
    public ResponseEntity<Page<StudentResponse>> getStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                studentService.search(keyword, page, size)
        );
    }

    /* ===================== GET BY ID ===================== */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                studentService.getById(id)
        );
    }

    /* ===================== CREATE ===================== */
    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody StudentRequest request
    ) {
        studentService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    /* ===================== UPDATE ===================== */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable UUID id,
            @Valid @RequestBody StudentRequest request
    ) {
        studentService.update(id, request);
        return ResponseEntity.ok().build();
    }

    /* ===================== DELETE ===================== */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ===================== PRINT / EXPORT ===================== */
    @GetMapping("/print")
    public ResponseEntity<List<StudentResponse>> getForPrint() {
        return ResponseEntity.ok(
                studentService.getForPrint()
        );
    }
}

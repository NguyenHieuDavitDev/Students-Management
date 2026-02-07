package com.example.stduents_management.classroom.controller;

import com.example.stduents_management.classroom.dto.ClassRequest;
import com.example.stduents_management.classroom.dto.ClassResponse;
import com.example.stduents_management.classroom.service.ClassService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    /* ================== SEARCH + PAGINATION ================== */
    @GetMapping
    public ResponseEntity<Page<ClassResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                classService.search(keyword, page, size)
        );
    }

    /* ================== GET BY ID ================== */
    @GetMapping("/{id}")
    public ResponseEntity<ClassResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                classService.getById(id)
        );
    }

    /* ================== CREATE ================== */
    @PostMapping
    public ResponseEntity<ClassResponse> create(
            @Valid @RequestBody ClassRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(classService.create(request));
    }

    /* ================== UPDATE ================== */
    @PutMapping("/{id}")
    public ResponseEntity<ClassResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ClassRequest request
    ) {
        return ResponseEntity.ok(
                classService.update(id, request)
        );
    }

    /* ================== DELETE ================== */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        classService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ================== EXPORT EXCEL ================== */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        classService.exportExcel(response);
    }

    /* ================== IMPORT EXCEL ================== */
    @PostMapping("/import")
    public ResponseEntity<String> importExcel(
            @RequestParam MultipartFile file
    ) {
        int count = classService.importExcel(file);
        return ResponseEntity.ok(
                "Đã import " + count + " lớp học"
        );
    }

    /* ================== PRINT ================== */
    @GetMapping("/print")
    public ResponseEntity<List<ClassResponse>> print() {
        return ResponseEntity.ok(
                classService.getForPrint()
        );
    }
}

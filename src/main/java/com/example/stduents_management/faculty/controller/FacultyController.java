package com.example.stduents_management.faculty.controller;

import com.example.stduents_management.faculty.dto.FacultyRequest;
import com.example.stduents_management.faculty.dto.FacultyResponse;
import com.example.stduents_management.faculty.service.FacultyService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping("/search")
    public Page<FacultyResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return facultyService.search(keyword, page, size);
    }

    @GetMapping("/{id}")
    public FacultyResponse get(@PathVariable UUID id) {
        return facultyService.getById(id);
    }

    @PostMapping
    public FacultyResponse create(@Valid @RequestBody FacultyRequest req) {
        return facultyService.create(req);
    }

    @PutMapping("/{id}")
    public FacultyResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody FacultyRequest req
    ) {
        return facultyService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        facultyService.delete(id);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        facultyService.exportExcel(response);
    }

    @PostMapping("/import")
    public int importExcel(@RequestParam MultipartFile file) {
        return facultyService.importExcel(file);
    }
}

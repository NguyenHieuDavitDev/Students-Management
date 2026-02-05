package com.example.stduents_management.faculty.controller;

import com.example.stduents_management.faculty.dto.FacultyRequest;
import com.example.stduents_management.faculty.dto.FacultyResponse;
import com.example.stduents_management.faculty.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/faculties")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping
    public List<FacultyResponse> getAll() {
        return facultyService.getAll();
    }

    @GetMapping("/search")
    public Page<FacultyResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return facultyService.search(keyword, page, size);
    }

    @GetMapping("/{id}")
    public FacultyResponse getById(@PathVariable UUID id) {
        return facultyService.getById(id);
    }

    @PostMapping
    public FacultyResponse create(
            @Valid @RequestBody FacultyRequest request
    ) {
        return facultyService.create(request);
    }

    @PutMapping("/{id}")
    public FacultyResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody FacultyRequest request
    ) {
        return facultyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        facultyService.delete(id);
    }
}

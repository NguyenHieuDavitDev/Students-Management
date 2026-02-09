package com.example.stduents_management.educationtype.controller;

import com.example.stduents_management.educationtype.dto.EducationTypeRequest;
import com.example.stduents_management.educationtype.dto.EducationTypeResponse;
import com.example.stduents_management.educationtype.service.EducationTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/education-types")
@RequiredArgsConstructor
public class EducationTypeController {

    private final EducationTypeService service;

    @GetMapping
    public ResponseEntity<Page<EducationTypeResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                service.search(keyword, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EducationTypeResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<EducationTypeResponse> create(
            @Valid @RequestBody EducationTypeRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EducationTypeResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody EducationTypeRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<EducationTypeResponse>> getAllForSelect() {
        return ResponseEntity.ok(service.getForPrint());
    }
}

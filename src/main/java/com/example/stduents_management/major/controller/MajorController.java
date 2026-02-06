package com.example.stduents_management.major.controller;

import com.example.stduents_management.major.dto.MajorRequest;
import com.example.stduents_management.major.dto.MajorResponse;
import com.example.stduents_management.major.service.MajorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/majors")
@RequiredArgsConstructor
public class MajorController {

    private final MajorService majorService;

    /* ================== GET LIST (SEARCH + PAGINATION) ================== */
    @GetMapping
    public ResponseEntity<Page<MajorResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                majorService.search(keyword, page, size)
        );
    }

    /* ================== GET BY ID ================== */
    @GetMapping("/{id}")
    public ResponseEntity<MajorResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                majorService.getById(id)
        );
    }

    /* ================== CREATE ================== */
    @PostMapping
    public ResponseEntity<MajorResponse> create(
            @Valid @RequestBody MajorRequest request
    ) {
        MajorResponse response = majorService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /* ================== UPDATE ================== */
    @PutMapping("/{id}")
    public ResponseEntity<MajorResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody MajorRequest request
    ) {
        return ResponseEntity.ok(
                majorService.update(id, request)
        );
    }

    /* ================== DELETE ================== */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        majorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ================== GET ALL (NO PAGING – DROPDOWN) ================== */
    @GetMapping("/all")
    public ResponseEntity<List<MajorResponse>> getAllForSelect() {
        return ResponseEntity.ok(
                majorService.getForPrint()
        );
    }
}

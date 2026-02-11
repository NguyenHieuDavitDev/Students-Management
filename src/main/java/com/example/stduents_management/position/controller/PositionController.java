package com.example.stduents_management.position.controller;

import com.example.stduents_management.position.dto.PositionRequest;
import com.example.stduents_management.position.dto.PositionResponse;
import com.example.stduents_management.position.service.PositionService;
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
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    /* ================= SEARCH + PAGINATION ================= */
    @GetMapping
    public ResponseEntity<Page<PositionResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                positionService.search(keyword, page, size)
        );
    }

    /* ================= GET BY ID ================= */
    @GetMapping("/{id}")
    public ResponseEntity<PositionResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                positionService.getById(id)
        );
    }

    /* ================= CREATE ================= */
    @PostMapping
    public ResponseEntity<PositionResponse> create(
            @Valid @RequestBody PositionRequest request
    ) {
        PositionResponse response =
                positionService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /* ================= UPDATE ================= */
    @PutMapping("/{id}")
    public ResponseEntity<PositionResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody PositionRequest request
    ) {
        return ResponseEntity.ok(
                positionService.update(id, request)
        );
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        positionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ================= EXPORT EXCEL ================= */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        positionService.exportExcel(response);
    }

    /* ================= IMPORT EXCEL ================= */
    @PostMapping("/import")
    public ResponseEntity<String> importExcel(
            @RequestParam MultipartFile file
    ) {
        int count = positionService.importExcel(file);
        return ResponseEntity.ok(
                "Đã import " + count + " chức danh"
        );
    }

    /* ================= PRINT (NO PAGING) ================= */
    @GetMapping("/print")
    public ResponseEntity<List<PositionResponse>> print() {
        return ResponseEntity.ok(
                positionService.getForPrint()
        );
    }

    /* ================= GET ALL (DROPDOWN) ================= */
    @GetMapping("/all")
    public ResponseEntity<List<PositionResponse>> getAllNoPaging() {
        return ResponseEntity.ok(
                positionService.getForPrint()
        );
    }
}

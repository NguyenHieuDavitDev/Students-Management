package com.example.stduents_management.trainingprogram.controller;

import com.example.stduents_management.trainingprogram.dto.TrainingProgramRequest;
import com.example.stduents_management.trainingprogram.dto.TrainingProgramResponse;
import com.example.stduents_management.trainingprogram.service.TrainingProgramService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/training-programs")
@RequiredArgsConstructor
public class TrainingProgramController {

    private final TrainingProgramService trainingProgramService;

    /* ================= SEARCH + PAGINATION ================= */
    @GetMapping
    public ResponseEntity<Page<TrainingProgramResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                trainingProgramService.search(keyword, page, size)
        );
    }

    /* ================= GET BY ID ================= */
    @GetMapping("/{id}")
    public ResponseEntity<TrainingProgramResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                trainingProgramService.getById(id)
        );
    }

    /* ================= CREATE ================= */
    @PostMapping
    public ResponseEntity<TrainingProgramResponse> create(
            @Valid @RequestBody TrainingProgramRequest request
    ) {
        TrainingProgramResponse response =
                trainingProgramService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /* ================= UPDATE ================= */
    @PutMapping("/{id}")
    public ResponseEntity<TrainingProgramResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TrainingProgramRequest request
    ) {
        return ResponseEntity.ok(
                trainingProgramService.update(id, request)
        );
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        trainingProgramService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ================= EXPORT EXCEL ================= */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        trainingProgramService.exportExcel(response);
    }

    /* ================= IMPORT EXCEL ================= */
    @PostMapping("/import")
    public ResponseEntity<String> importExcel(
            @RequestParam org.springframework.web.multipart.MultipartFile file
    ) {
        int count = trainingProgramService.importExcel(file);
        return ResponseEntity.ok(
                "Đã import " + count + " chương trình đào tạo"
        );
    }

    /* ================= PRINT (NO PAGING) ================= */
    @GetMapping("/print")
    public ResponseEntity<List<TrainingProgramResponse>> print() {
        return ResponseEntity.ok(
                trainingProgramService.getForPrint()
        );
    }

    /* ================= GET ALL (DROPDOWN) ================= */
    @GetMapping("/all")
    public ResponseEntity<List<TrainingProgramResponse>> getAllNoPaging() {
        return ResponseEntity.ok(
                trainingProgramService.getForPrint()
        );
    }
}

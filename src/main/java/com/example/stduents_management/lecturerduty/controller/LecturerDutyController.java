package com.example.stduents_management.lecturerduty.controller;

import com.example.stduents_management.lecturerduty.dto.LecturerDutyRequest;
import com.example.stduents_management.lecturerduty.dto.LecturerDutyResponse;
import com.example.stduents_management.lecturerduty.service.LecturerDutyService;
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
@RequestMapping("/api/lecturer-duties")
@RequiredArgsConstructor
public class LecturerDutyController {

    private final LecturerDutyService lecturerDutyService;

    @GetMapping
    public ResponseEntity<Page<LecturerDutyResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                lecturerDutyService.search(keyword, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LecturerDutyResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                lecturerDutyService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<LecturerDutyResponse> create(
            @Valid @RequestBody LecturerDutyRequest request
    ) {
        LecturerDutyResponse response =
                lecturerDutyService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LecturerDutyResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody LecturerDutyRequest request
    ) {
        return ResponseEntity.ok(
                lecturerDutyService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        lecturerDutyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        lecturerDutyService.exportExcel(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importExcel(
            @RequestParam MultipartFile file
    ) {
        int count = lecturerDutyService.importExcel(file);
        return ResponseEntity.ok(
                "Đã import " + count + " chức vụ"
        );
    }

    @GetMapping("/print")
    public ResponseEntity<List<LecturerDutyResponse>> print() {
        return ResponseEntity.ok(
                lecturerDutyService.getForPrint()
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<LecturerDutyResponse>> getAllNoPaging() {
        return ResponseEntity.ok(
                lecturerDutyService.getForPrint()
        );
    }
}

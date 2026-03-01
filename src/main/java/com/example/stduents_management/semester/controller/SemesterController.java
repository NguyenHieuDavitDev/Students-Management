package com.example.stduents_management.semester.controller;

import com.example.stduents_management.semester.dto.SemesterRequest;
import com.example.stduents_management.semester.dto.SemesterResponse;
import com.example.stduents_management.semester.service.SemesterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping
    public ResponseEntity<Page<SemesterResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(semesterService.search(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SemesterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(semesterService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody SemesterRequest req) {
        semesterService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody SemesterRequest req
    ) {
        semesterService.update(id, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        semesterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/print")
    public ResponseEntity<List<SemesterResponse>> print() {
        return ResponseEntity.ok(semesterService.getForPrint());
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        semesterService.importExcel(file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {

        byte[] data = semesterService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=semesters.xlsx");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
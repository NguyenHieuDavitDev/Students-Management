package com.example.stduents_management.lecturer.controller;

import com.example.stduents_management.lecturer.dto.LecturerRequest;
import com.example.stduents_management.lecturer.dto.LecturerResponse;
import com.example.stduents_management.lecturer.service.LecturerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lecturers")
@RequiredArgsConstructor
public class LecturerController {

    private final LecturerService lecturerService;

    @GetMapping
    public ResponseEntity<Page<LecturerResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                lecturerService.search(keyword, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LecturerResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(lecturerService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody LecturerRequest req) {
        lecturerService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable UUID id,
            @Valid @RequestBody LecturerRequest req
    ) {
        lecturerService.update(id, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lecturerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/print")
    public ResponseEntity<List<LecturerResponse>> print() {
        return ResponseEntity.ok(lecturerService.getForPrint());
    }
}

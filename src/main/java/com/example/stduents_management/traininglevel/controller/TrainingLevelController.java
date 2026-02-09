package com.example.stduents_management.traininglevel.controller;

import com.example.stduents_management.traininglevel.dto.TrainingLevelRequest;
import com.example.stduents_management.traininglevel.dto.TrainingLevelResponse;
import com.example.stduents_management.traininglevel.service.TrainingLevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/training-levels")
@RequiredArgsConstructor
public class TrainingLevelController {

    private final TrainingLevelService trainingLevelService;

    @GetMapping
    public ResponseEntity<Page<TrainingLevelResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                trainingLevelService.search(keyword, page, size)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingLevelResponse> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                trainingLevelService.getById(id)
        );
    }

    @PostMapping
    public ResponseEntity<TrainingLevelResponse> create(
            @Valid @RequestBody TrainingLevelRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(trainingLevelService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingLevelResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TrainingLevelRequest request
    ) {
        return ResponseEntity.ok(
                trainingLevelService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        trainingLevelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrainingLevelResponse>> getAllForSelect() {
        return ResponseEntity.ok(
                trainingLevelService.getForPrint()
        );
    }
}

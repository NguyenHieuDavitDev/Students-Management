package com.example.stduents_management.feedback.controller;

import com.example.stduents_management.feedback.dto.FeedbackRequest;
import com.example.stduents_management.feedback.dto.FeedbackResponse;
import com.example.stduents_management.feedback.service.FeedbackService;
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
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public ResponseEntity<Page<FeedbackResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(feedbackService.search(keyword, fromDate, toDate, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(feedbackService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody FeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody FeedbackRequest request
    ) {
        return ResponseEntity.ok(feedbackService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        feedbackService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        feedbackService.exportExcel(response);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importExcel(@RequestParam MultipartFile file) {
        int count = feedbackService.importExcel(file);
        return ResponseEntity.ok("Đã import " + count + " phản hồi");
    }

    @GetMapping("/print")
    public ResponseEntity<List<FeedbackResponse>> print(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        return ResponseEntity.ok(feedbackService.getAllFiltered(keyword, fromDate, toDate));
    }
}

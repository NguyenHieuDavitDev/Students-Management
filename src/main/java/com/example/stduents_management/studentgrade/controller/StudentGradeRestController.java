package com.example.stduents_management.studentgrade.controller;

import com.example.stduents_management.studentgrade.dto.StudentGradeRequest;
import com.example.stduents_management.studentgrade.dto.StudentGradeResponse;
import com.example.stduents_management.studentgrade.service.StudentGradeService;
import com.example.stduents_management.user.service.CurrentUserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/student-grades")
@RequiredArgsConstructor
public class StudentGradeRestController {

    private final StudentGradeService studentGradeService;
    private final CurrentUserProfileService currentUserProfileService;

    @GetMapping
    public ResponseEntity<Page<StudentGradeResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long courseClassId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(studentGradeService.search(keyword, courseClassId, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentGradeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(studentGradeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody StudentGradeRequest request) {
        Optional<UUID> lecturerId = Optional.ofNullable(request.getGradedByLecturerId())
                .or(() -> currentUserProfileService.getCurrentLecturerId());
        studentGradeService.save(request, lecturerId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody StudentGradeRequest request) {
        Optional<UUID> lecturerId = Optional.ofNullable(request.getGradedByLecturerId())
                .or(() -> currentUserProfileService.getCurrentLecturerId());
        studentGradeService.save(request, lecturerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        studentGradeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/print")
    public ResponseEntity<List<StudentGradeResponse>> getForPrint(
            @RequestParam(required = false) Long courseClassId
    ) {
        List<StudentGradeResponse> items = courseClassId != null
                ? studentGradeService.getForPrintByCourseClass(courseClassId)
                : studentGradeService.getForPrint();
        return ResponseEntity.ok(items);
    }
}

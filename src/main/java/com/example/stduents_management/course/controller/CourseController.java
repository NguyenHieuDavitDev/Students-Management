package com.example.stduents_management.course.controller;

import com.example.stduents_management.course.dto.CourseRequest;
import com.example.stduents_management.course.dto.CourseResponse;
import com.example.stduents_management.course.service.CourseService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /* ================= SEARCH + PAGING ================= */
    @GetMapping
    public ResponseEntity<Page<CourseResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CourseResponse> result = courseService.search(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    /* ================= GET BY ID ================= */
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getById(@PathVariable UUID id) {
        CourseResponse resp = courseService.getById(id);
        return ResponseEntity.ok(resp);
    }

    /* ================= CREATE ================= */
    @PostMapping
    public ResponseEntity<CourseResponse> create(
            @RequestBody CourseRequest request
    ) {
        CourseResponse resp = courseService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /* ================= UPDATE ================= */
    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> update(
            @PathVariable UUID id,
            @RequestBody CourseRequest request
    ) {
        CourseResponse resp = courseService.update(id, request);
        return ResponseEntity.ok(resp);
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ================= EXPORT =================
       Note: export writes directly to response output stream.
       Controller just delegates and returns nothing (void).
    */
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try {
            // set response headers early to avoid view rendering later
            response.reset();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=courses.xlsx");

            courseService.exportExcel(response);
            // service writes and flushes stream
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể export Excel");
        }
    }

    /* ================= IMPORT ================= */
    @PostMapping("/import")
    public ResponseEntity<String> importExcel(
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng upload file Excel (.xlsx)");
        }
        int count = courseService.importExcel(file);
        return ResponseEntity.ok("Đã import " + count + " học phần");
    }

    /* ================= PRINT ================= */
    @GetMapping("/print")
    public ResponseEntity<List<CourseResponse>> print() {
        List<CourseResponse> list = courseService.getForPrint();
        return ResponseEntity.ok(list);
    }
}
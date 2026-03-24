package com.example.stduents_management.attendance.controller;

import com.example.stduents_management.attendance.dto.AttendanceRequest;
import com.example.stduents_management.attendance.dto.AttendanceResponse;
import com.example.stduents_management.attendance.service.AttendanceService;
import com.example.stduents_management.user.service.CurrentUserProfileService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceRestController {

    private final AttendanceService attendanceService;
    private final CurrentUserProfileService currentUserProfileService;

    @GetMapping
    public ResponseEntity<Page<AttendanceResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long courseClassId,
            @RequestParam(required = false) String attendanceDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDate date = AttendanceService.parseDate(attendanceDate);
        return ResponseEntity.ok(attendanceService.search(keyword, courseClassId, date, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> create(
            @Valid @RequestBody AttendanceRequest request
    ) {
        Optional<UUID> lecturerId = request.getMarkedByLecturerId() != null
                ? Optional.of(request.getMarkedByLecturerId())
                : currentUserProfileService.getCurrentLecturerId();

        AttendanceResponse saved = attendanceService.upsert(null, request, lecturerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AttendanceRequest request
    ) {
        Optional<UUID> lecturerId = request.getMarkedByLecturerId() != null
                ? Optional.of(request.getMarkedByLecturerId())
                : currentUserProfileService.getCurrentLecturerId();

        AttendanceResponse saved = attendanceService.upsert(id, request, lecturerId);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize")
    public ResponseEntity<String> initialize(
            @RequestParam Long courseClassId,
            @RequestParam String attendanceDate,
            @RequestParam(required = false) UUID markedByLecturerId
    ) {
        LocalDate date = AttendanceService.parseDate(attendanceDate);
        Optional<UUID> lecturerId = markedByLecturerId != null
                ? Optional.of(markedByLecturerId)
                : currentUserProfileService.getCurrentLecturerId();

        attendanceService.initializeForClassAndDate(courseClassId, date, lecturerId);
        return ResponseEntity.ok("Đã khởi tạo bản ghi điểm danh thành công");
    }

    @GetMapping("/export")
    public void exportExcel(
            HttpServletResponse response,
            @RequestParam Long courseClassId,
            @RequestParam String attendanceDate
    ) {
        LocalDate date = AttendanceService.parseDate(attendanceDate);
        attendanceService.exportExcel(response, courseClassId, date);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importExcel(
            @RequestParam MultipartFile file,
            @RequestParam Long courseClassId,
            @RequestParam String attendanceDate,
            @RequestParam(required = false) UUID markedByLecturerId
    ) {
        LocalDate date = AttendanceService.parseDate(attendanceDate);
        Optional<UUID> lecturerId = markedByLecturerId != null
                ? Optional.of(markedByLecturerId)
                : currentUserProfileService.getCurrentLecturerId();
        int count = attendanceService.importExcel(file, courseClassId, date, lecturerId);
        return ResponseEntity.ok("Đã import " + count + " bản ghi điểm danh");
    }

    @GetMapping("/print")
    public ResponseEntity<List<AttendanceResponse>> print(
            @RequestParam Long courseClassId,
            @RequestParam String attendanceDate
    ) {
        LocalDate date = AttendanceService.parseDate(attendanceDate);
        return ResponseEntity.ok(attendanceService.getForPrint(courseClassId, date));
    }
}


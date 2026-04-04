package com.example.stduents_management.schedule.controller;

import com.example.stduents_management.schedule.dto.ScheduleCalendarEventResponse;
import com.example.stduents_management.schedule.dto.ScheduleCalendarMetaResponse;
import com.example.stduents_management.schedule.dto.ScheduleRequest;
import com.example.stduents_management.schedule.dto.ScheduleResponse;
import com.example.stduents_management.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/schedules/api")
@RequiredArgsConstructor
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    @GetMapping("/meta")
    public ScheduleCalendarMetaResponse meta() {
        return scheduleService.getCalendarMeta();
    }

    @GetMapping("/events")
    public List<ScheduleCalendarEventResponse> events(
            @RequestParam Long semesterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return scheduleService.listCalendarEvents(semesterId, start, end);
    }

    @GetMapping("/{id}")
    public ScheduleResponse get(@PathVariable UUID id) {
        return scheduleService.getById(id);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody ScheduleRequest req) {
        UUID id = scheduleService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id.toString()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody ScheduleRequest req) {
        scheduleService.update(id, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

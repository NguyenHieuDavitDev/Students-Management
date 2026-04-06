package com.example.stduents_management.schedule.controller;

import com.example.stduents_management.schedule.dto.CalendarRescheduleOnceRequest;
import com.example.stduents_management.schedule.dto.CalendarRoomOverrideRequest;
import com.example.stduents_management.schedule.dto.CalendarTimeSlotOverrideRequest;
import com.example.stduents_management.schedule.dto.ScheduleCalendarEventResponse;
import com.example.stduents_management.schedule.dto.ScheduleCalendarMetaResponse;
import com.example.stduents_management.schedule.dto.ScheduleRequest;
import com.example.stduents_management.schedule.dto.ScheduleResponse;
import com.example.stduents_management.schedule.service.ScheduleService;
import com.example.stduents_management.scheduleoverride.dto.ScheduleOverrideRequest;
import com.example.stduents_management.scheduleoverride.entity.OverrideType;
import com.example.stduents_management.scheduleoverride.service.ScheduleOverrideService;
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
    private final ScheduleOverrideService scheduleOverrideService;

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

    /**
     * Đổi phòng cho đúng một ngày (không sửa dòng lịch gốc). Dùng sau khi kéo thả nếu chọn “chỉ đổi phòng buổi này”.
     */
    @PostMapping("/{scheduleId}/room-override")
    public ResponseEntity<Void> createRoomOverrideForDate(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody CalendarRoomOverrideRequest body
    ) {
        ScheduleOverrideRequest req = new ScheduleOverrideRequest();
        req.setScheduleId(scheduleId);
        req.setOverrideDate(body.overrideDate());
        req.setOverrideType(OverrideType.ROOM_CHANGE);
        req.setNewRoomId(body.newRoomId());
        scheduleOverrideService.create(req);
        return ResponseEntity.ok().build();
    }

    /** Đổi tiết cho đúng một ngày (cùng ngày trên lịch). */
    @PostMapping("/{scheduleId}/time-slot-override")
    public ResponseEntity<Void> timeSlotOverrideForDate(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody CalendarTimeSlotOverrideRequest body
    ) {
        scheduleOverrideService.replaceOccurrenceTimeSlot(scheduleId, body.overrideDate(), body.newTimeSlotId());
        return ResponseEntity.ok().build();
    }

    /** Dịch một buổi sang ngày + tiết đích; lịch gốc không đổi. */
    @PostMapping("/{scheduleId}/reschedule-once")
    public ResponseEntity<Void> rescheduleOnce(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody CalendarRescheduleOnceRequest body
    ) {
        scheduleOverrideService.replaceOccurrenceReschedule(
                scheduleId,
                body.originalDate(),
                body.movedToDate(),
                body.newTimeSlotId(),
                body.newRoomId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.stduents_management.schedule.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Một ô trên FullCalendar (một tuần × một dòng lịch trong DB).
 * {@code extendedProps} phải chứa {@code scheduleId} để kéo thả / click sửa.
 */
public record ScheduleCalendarEventResponse(
        String id,
        String title,
        LocalDateTime start,
        LocalDateTime end,
        String backgroundColor,
        Map<String, Object> extendedProps
) {
    public ScheduleCalendarEventResponse {
        extendedProps = extendedProps == null ? Map.of() : Map.copyOf(extendedProps);
    }
}

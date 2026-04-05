package com.example.stduents_management.schedule.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
        // Map.copyOf ném NPE nếu có value null — calendarExtendedProps hay có note/status null.
        if (extendedProps == null || extendedProps.isEmpty()) {
            extendedProps = Map.of();
        } else {
            Map<String, Object> cleaned = new LinkedHashMap<>();
            extendedProps.forEach((k, v) -> {
                if (k != null && v != null) {
                    cleaned.put(k, v);
                }
            });
            extendedProps = Map.copyOf(cleaned);
        }
    }
}

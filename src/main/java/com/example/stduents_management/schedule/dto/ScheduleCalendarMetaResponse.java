package com.example.stduents_management.schedule.dto;

import java.util.List;


public record ScheduleCalendarMetaResponse(
        List<SemesterRow> semesters,
        List<ClassSectionRow> classSections,
        List<LecturerRow> lecturers,
        List<RoomRow> rooms,
        List<TimeSlotRow> timeSlots,
        List<String> weekPatterns,
        List<String> sessionTypes,
        List<String> scheduleTypes,
        List<String> scheduleStatuses
) {
    public record SemesterRow(Long id, String code, String name, String startDate) {}

    public record ClassSectionRow(Long id, String classCode, String className, Long semesterId) {}

    public record LecturerRow(String lecturerId, String lecturerCode, String fullName) {}

    public record RoomRow(Long roomId, String roomCode, String roomName) {}

    public record TimeSlotRow(
            Integer id,
            String slotCode,
            String startTime,
            String endTime,
            Integer periodStart,
            Integer periodEnd
    ) {}
}

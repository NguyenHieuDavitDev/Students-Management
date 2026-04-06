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
    /** Thứ Hai mốc tuần 1 (khớp ScheduleService.semesterAnchorMonday; dùng khi tính tuần trên calendar). */
    public record SemesterRow(Long id, String code, String name, String startDate, String weekAnchorMonday) {}

    public record ClassSectionRow(Long id, String classCode, String className, Long semesterId) {}

    public record LecturerRow(String lecturerId, String lecturerCode, String fullName) {}

    public record RoomRow(Long roomId, String roomCode, String roomName) {}

    /**
     * @param dayPart      {@code MORNING} | {@code AFTERNOON} | {@code EVENING}
     * @param dayPartLabel nhãn tiếng Việt cho lưới lịch (buổi sáng / chiều / tối)
     */
    public record TimeSlotRow(
            Integer id,
            String slotCode,
            String startTime,
            String endTime,
            Integer periodStart,
            Integer periodEnd,
            String dayPart,
            String dayPartLabel
    ) {}
}

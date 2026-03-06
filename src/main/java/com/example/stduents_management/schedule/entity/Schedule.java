package com.example.stduents_management.schedule.entity;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.semester.entity.Semester;
import com.example.stduents_management.timeslot.entity.TimeSlot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_section_id", nullable = false)
    private ClassSection classSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    /** 2=Thứ 2, 3=Thứ 3, ..., 8=Chủ nhật */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_week", nullable = false)
    private Integer startWeek;

    @Column(name = "end_week", nullable = false)
    private Integer endWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_pattern", length = 20, nullable = false)
    private WeekPattern weekPattern = WeekPattern.ALL;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", length = 20, nullable = false)
    private SessionType sessionType = SessionType.THEORY;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", length = 20, nullable = false)
    private ScheduleType scheduleType = ScheduleType.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ScheduleStatus status = ScheduleStatus.ACTIVE;

    @Column(name = "note", columnDefinition = "NVARCHAR(255)")
    private String note;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

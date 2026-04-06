package com.example.stduents_management.scheduleoverride.entity;

import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.schedule.entity.Schedule;
import com.example.stduents_management.timeslot.entity.TimeSlot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_overrides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "override_id", columnDefinition = "uniqueidentifier")
    private UUID overrideId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Schedule schedule;

    @Column(name = "override_date", nullable = false)
    private LocalDate overrideDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "override_type", nullable = false, length = 30)
    private OverrideType overrideType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_room_id")
    private Room newRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_time_slot_id")
    private TimeSlot newTimeSlot;

    /** Với {@link OverrideType#RESCHEDULE}: ngày đích (buổi học xuất hiện tại đây thay vì {@link #overrideDate}). */
    @Column(name = "moved_to_date")
    private LocalDate movedToDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_lecturer_id", columnDefinition = "uniqueidentifier")
    private Lecturer newLecturer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OverrideStatus status = OverrideStatus.ACTIVE;

    @Column(name = "reason", columnDefinition = "NVARCHAR(255)")
    private String reason;

    @Column(name = "approved_by", columnDefinition = "uniqueidentifier")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

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

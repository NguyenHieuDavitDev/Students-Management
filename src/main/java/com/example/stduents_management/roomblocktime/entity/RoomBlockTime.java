package com.example.stduents_management.roomblocktime.entity;

import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.timeslot.entity.TimeSlot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng khóa phòng (room_block_times).
 * Lưu các khung thời gian/phòng bị khóa (bảo trì, sự kiện, thi, v.v.).
 */
@Entity
@Table(name = "room_block_times")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomBlockTime {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "block_id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID blockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false, length = 30, columnDefinition = "NVARCHAR(30)")
    private BlockType blockType;

    /** Thứ trong tuần: 2=Thứ 2, 3=Thứ 3, ..., 8=Chủ nhật (theo spec 2-8). */
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    /** Tuần bắt đầu (theo năm học / học kỳ). */
    @Column(name = "start_week")
    private Integer startWeek;

    @Column(name = "end_week")
    private Integer endWeek;

    /** Khóa theo khoảng ngày (nếu dùng thay cho tuần). */
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "reason", length = 255, columnDefinition = "NVARCHAR(255)")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private BlockStatus status = BlockStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = BlockStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

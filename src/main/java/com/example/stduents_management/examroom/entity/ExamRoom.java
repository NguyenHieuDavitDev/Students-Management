package com.example.stduents_management.examroom.entity;

import com.example.stduents_management.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng phân phòng thi (exam_rooms).
 * Gắn phòng học (Room) vào danh sách phòng dùng cho thi.
 * Mỗi phòng chỉ xuất hiện tối đa một lần trong danh sách phòng thi.
 */
@Entity
@Table(
    name = "exam_rooms",
    uniqueConstraints = @UniqueConstraint(columnNames = "room_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "exam_capacity")
    private Integer examCapacity;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

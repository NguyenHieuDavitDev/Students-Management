package com.example.stduents_management.room.entity;

import com.example.stduents_management.building.entity.Building;
import com.example.stduents_management.roomtype.entity.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "room_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long roomId;

    // NVARCHAR(20)
    @Column(
            name = "room_code",
            nullable = false,
            columnDefinition = "NVARCHAR(20)"
    )
    private String roomCode;

    // NVARCHAR(100)
    @Column(
            name = "room_name",
            nullable = false,
            columnDefinition = "NVARCHAR(100)"
    )
    private String roomName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "area")
    private Double area;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            columnDefinition = "NVARCHAR(20)"
    )
    private RoomStatus status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.isActive == null) {
            this.isActive = Boolean.TRUE;
        }

        if (this.status == null) {
            this.status = RoomStatus.AVAILABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
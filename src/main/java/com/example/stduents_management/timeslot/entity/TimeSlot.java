package com.example.stduents_management.timeslot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(
        name = "time_slots",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "slot_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "slot_code", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private String slotCode;

    @Column(name = "period_start", nullable = false)
    private Integer periodStart;

    @Column(name = "period_end", nullable = false)
    private Integer periodEnd;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

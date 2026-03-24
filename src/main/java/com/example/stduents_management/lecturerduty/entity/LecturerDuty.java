package com.example.stduents_management.lecturerduty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "lecturer_duties",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "duty_code"),
                @UniqueConstraint(columnNames = "duty_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LecturerDuty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "lecturer_duty_id", columnDefinition = "uniqueidentifier")
    private UUID lecturerDutyId;

    @Column(name = "duty_code", nullable = false, columnDefinition = "NVARCHAR(20)")
    private String dutyCode;

    @Column(name = "duty_name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String dutyName;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;
}

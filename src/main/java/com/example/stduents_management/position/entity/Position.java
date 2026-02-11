package com.example.stduents_management.position.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "positions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "position_code"),
                @UniqueConstraint(columnNames = "position_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID positionId;

    @Column(
            name = "position_code",
            nullable = false,
            columnDefinition = "NVARCHAR(20)"
    )
    private String positionCode;

    @Column(
            name = "position_name",
            nullable = false,
            columnDefinition = "NVARCHAR(150)"
    )
    private String positionName;

    @Column(
            name = "description",
            columnDefinition = "NVARCHAR(500)"
    )
    private String description;
}

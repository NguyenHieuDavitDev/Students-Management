package com.example.stduents_management.building.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "buildings",
        uniqueConstraints = @UniqueConstraint(columnNames = "building_code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID buildingId;

    @Column(name = "building_code", nullable = false, length = 20)
    private String buildingCode;

    @Column(name = "building_name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String buildingName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    private Integer numberOfFloors;

    private Double totalArea;

    private String description;
}

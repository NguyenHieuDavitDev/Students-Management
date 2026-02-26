package com.example.stduents_management.roomtype.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "room_types",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "room_type_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID roomTypeId;

    @Column(name = "room_type_code", nullable = false, length = 20)
    private String roomTypeCode;

    @Column(name = "room_type_name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String roomTypeName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;

    private Integer maxCapacity;
}
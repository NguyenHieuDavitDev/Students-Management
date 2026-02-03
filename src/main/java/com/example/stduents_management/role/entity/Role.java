package com.example.stduents_management.role.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    // NVARCHAR
    @Column(
            nullable = false,
            unique = true,
            columnDefinition = "NVARCHAR(100)"
    )
    private String name;

    // NVARCHAR
    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;
}


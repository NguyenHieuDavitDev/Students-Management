package com.example.stduents_management.educationtype.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "education_types",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"education_type_name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EducationType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID educationTypeId;

    @Column(
            name = "education_type_name",
            nullable = false,
            columnDefinition = "NVARCHAR(100)"
    )
    private String educationTypeName;

    @Column(nullable = false)
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void preCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

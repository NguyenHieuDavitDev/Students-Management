package com.example.stduents_management.faculty.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "faculties",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "faculty_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID facultyId;

    @Column(
            name = "faculty_code",
            nullable = false,
            unique = true,
            columnDefinition = "NVARCHAR(50)"
    )
    private String facultyCode;

    @Column(
            name = "faculty_name",
            nullable = false,
            columnDefinition = "NVARCHAR(150)"
    )
    private String facultyName;
}

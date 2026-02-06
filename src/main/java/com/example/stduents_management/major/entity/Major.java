package com.example.stduents_management.major.entity;

import com.example.stduents_management.faculty.entity.Faculty;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "majors",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"major_name", "faculty_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID majorId;

    @Column(
            name = "major_name",
            nullable = false,
            columnDefinition = "NVARCHAR(150)"
    )
    private String majorName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "faculty_id",
            nullable = false,
            columnDefinition = "uniqueidentifier"
    )
    private Faculty faculty;
}

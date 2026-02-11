package com.example.stduents_management.lecturer.entity;

import com.example.stduents_management.faculty.entity.Faculty;
import com.example.stduents_management.position.entity.Position;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "lecturers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "lecturer_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lecturer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID lecturerId;

    @Column(name = "lecturer_code", nullable = false, columnDefinition = "VARCHAR(20)")
    private String lecturerCode;

    @Column(name = "full_name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String fullName;

    private LocalDate dateOfBirth;

    @Column(columnDefinition = "NVARCHAR(10)")
    private String gender;

    @Column(name = "citizen_id", columnDefinition = "VARCHAR(20)")
    private String citizenId;

    private String email;
    private String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(columnDefinition = "VARCHAR(255)")
    private String avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;   // Học vị

    @Column(columnDefinition = "NVARCHAR(50)")
    private String academicTitle;    // Học hàm

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;
}

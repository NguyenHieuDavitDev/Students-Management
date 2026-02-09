package com.example.stduents_management.student.entity;

import com.example.stduents_management.classroom.entity.ClassEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "student_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID studentId;

    @Column(
            name = "student_code",
            nullable = false,
            columnDefinition = "VARCHAR(20)"
    )
    private String studentCode;

    @Column(
            name = "full_name",
            nullable = false,
            columnDefinition = "NVARCHAR(150)"
    )
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(columnDefinition = "NVARCHAR(10)")
    private String gender;

    @Column(name = "citizen_id", columnDefinition = "VARCHAR(20)")
    private String citizenId;

    @Column(columnDefinition = "VARCHAR(150)")
    private String email;

    @Column(name = "phone_number", columnDefinition = "VARCHAR(20)")
    private String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(columnDefinition = "VARCHAR(255)")
    private String avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "class_id",
            nullable = false,
            columnDefinition = "uniqueidentifier"
    )
    private ClassEntity clazz;
}

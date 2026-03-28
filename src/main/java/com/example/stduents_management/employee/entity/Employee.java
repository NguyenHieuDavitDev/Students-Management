package com.example.stduents_management.employee.entity;

import com.example.stduents_management.department.entity.Department;
import com.example.stduents_management.position.entity.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "employees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "employee_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "employee_id", columnDefinition = "uniqueidentifier")
    private UUID employeeId;

    @Column(name = "employee_code", nullable = false, columnDefinition = "VARCHAR(30)")
    private String employeeCode;

    @Column(name = "full_name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", columnDefinition = "NVARCHAR(10)")
    private String gender;

    @Column(name = "citizen_id", columnDefinition = "VARCHAR(20)")
    private String citizenId;

    @Column(name = "email", columnDefinition = "NVARCHAR(150)")
    private String email;

    @Column(name = "phone_number", columnDefinition = "VARCHAR(30)")
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "avatar", columnDefinition = "VARCHAR(255)")
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, columnDefinition = "VARCHAR(30)")
    private EmployeeType employeeType = EmployeeType.OTHER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20)")
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}


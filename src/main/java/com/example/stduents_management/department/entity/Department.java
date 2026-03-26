package com.example.stduents_management.department.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "department_code"),
                @UniqueConstraint(columnNames = "department_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID departmentId;

    @Column(name = "department_code", nullable = false, columnDefinition = "NVARCHAR(30)")
    private String departmentCode;

    @Column(name = "department_name", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String departmentName;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;
}

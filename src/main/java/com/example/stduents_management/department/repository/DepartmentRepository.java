package com.example.stduents_management.department.repository;

import com.example.stduents_management.department.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    boolean existsByDepartmentCodeIgnoreCase(String code);

    boolean existsByDepartmentNameIgnoreCase(String name);

    boolean existsByDepartmentCodeIgnoreCaseAndDepartmentIdNot(String code, UUID id);

    boolean existsByDepartmentNameIgnoreCaseAndDepartmentIdNot(String name, UUID id);

    Page<Department> findByDepartmentCodeContainingIgnoreCaseOrDepartmentNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );
}

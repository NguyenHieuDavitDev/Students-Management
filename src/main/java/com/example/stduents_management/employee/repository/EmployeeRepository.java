package com.example.stduents_management.employee.repository;

import com.example.stduents_management.employee.entity.Employee;
import com.example.stduents_management.employee.entity.EmployeeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    boolean existsByEmployeeCodeIgnoreCase(String employeeCode);
    Optional<Employee> findByEmployeeCodeIgnoreCase(String employeeCode);

    boolean existsByEmployeeCodeIgnoreCaseAndEmployeeIdNot(String employeeCode, UUID employeeId);

    Page<Employee> findByEmployeeCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String employeeCode,
            String fullName,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Employee e
            WHERE (:keyword IS NULL OR :keyword = '' OR
                   LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(e.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:type IS NULL OR e.employeeType = :type)
            """)
    Page<Employee> search(@Param("keyword") String keyword, @Param("type") EmployeeType type, Pageable pageable);
}


package com.example.stduents_management.employee.repository;

import com.example.stduents_management.employee.entity.EmployeePositionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeePositionHistoryRepository extends JpaRepository<EmployeePositionHistory, UUID> {

    Optional<EmployeePositionHistory> findFirstByEmployee_EmployeeIdAndEffectiveToIsNullOrderByEffectiveFromDesc(
            UUID employeeId);
}

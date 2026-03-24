package com.example.stduents_management.lecturerduty.repository;

import com.example.stduents_management.lecturerduty.entity.LecturerDuty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LecturerDutyRepository extends JpaRepository<LecturerDuty, UUID> {

    boolean existsByDutyCodeIgnoreCase(String code);

    boolean existsByDutyNameIgnoreCase(String name);

    boolean existsByDutyCodeIgnoreCaseAndLecturerDutyIdNot(String code, UUID id);

    boolean existsByDutyNameIgnoreCaseAndLecturerDutyIdNot(String name, UUID id);

    Page<LecturerDuty> findByDutyCodeContainingIgnoreCaseOrDutyNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );
}

package com.example.stduents_management.major.repository;

import com.example.stduents_management.major.entity.Major;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MajorRepository extends JpaRepository<Major, UUID> {

    boolean existsByMajorNameIgnoreCaseAndFaculty_FacultyId(
            String majorName,
            UUID facultyId
    );

    boolean existsByMajorNameIgnoreCaseAndFaculty_FacultyIdAndMajorIdNot(
            String majorName,
            UUID facultyId,
            UUID majorId
    );

    Page<Major> findByMajorNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    java.util.Optional<Major> findByMajorName(String majorName);
}

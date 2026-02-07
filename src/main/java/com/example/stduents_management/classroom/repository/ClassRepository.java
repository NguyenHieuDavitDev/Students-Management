package com.example.stduents_management.classroom.repository;

import com.example.stduents_management.classroom.entity.ClassEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClassRepository extends JpaRepository<ClassEntity, UUID> {

    boolean existsByClassCodeIgnoreCaseAndAcademicYear(
            String classCode,
            String academicYear
    );

    boolean existsByClassCodeIgnoreCaseAndAcademicYearAndClassIdNot(
            String classCode,
            String academicYear,
            UUID classId
    );

    Page<ClassEntity> findByClassNameContainingIgnoreCaseOrClassCodeContainingIgnoreCase(
            String name,
            String code,
            Pageable pageable
    );
}

package com.example.stduents_management.educationtype.repository;



import com.example.stduents_management.educationtype.entity.EducationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EducationTypeRepository
        extends JpaRepository<EducationType, UUID> {

    boolean existsByEducationTypeNameIgnoreCase(String name);

    boolean existsByEducationTypeNameIgnoreCaseAndEducationTypeIdNot(
            String name,
            UUID id
    );

    Page<EducationType> findByEducationTypeNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    java.util.Optional<EducationType> findByEducationTypeName(String educationTypeName);
}


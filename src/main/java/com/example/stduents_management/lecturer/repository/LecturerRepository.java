package com.example.stduents_management.lecturer.repository;

import com.example.stduents_management.lecturer.entity.Lecturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LecturerRepository extends JpaRepository<Lecturer, UUID> {

    Optional<Lecturer> findByLecturerCodeIgnoreCase(String lecturerCode);

    boolean existsByLecturerCode(String lecturerCode);

    boolean existsByLecturerCodeAndLecturerIdNot(String lecturerCode, UUID id);

    Page<Lecturer> findByLecturerCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

    boolean existsByPosition_PositionId(UUID positionId);
}

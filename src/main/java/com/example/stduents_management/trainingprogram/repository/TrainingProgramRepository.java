package com.example.stduents_management.trainingprogram.repository;

import com.example.stduents_management.trainingprogram.entity.TrainingProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, UUID> {

    boolean existsByProgramCodeIgnoreCaseAndMajor_MajorIdAndCourseIgnoreCase(
            String programCode,
            UUID majorId,
            String course
    );

    boolean existsByProgramCodeIgnoreCaseAndMajor_MajorIdAndCourseIgnoreCaseAndProgramIdNot(
            String programCode,
            UUID majorId,
            String course,
            UUID programId
    );

    Page<TrainingProgram> findByProgramCodeContainingIgnoreCaseOrProgramNameContainingIgnoreCaseOrCourseContainingIgnoreCase(
            String programCode,
            String programName,
            String course,
            Pageable pageable
    );

    Page<TrainingProgram> findByMajor_MajorNameContainingIgnoreCase(
            String majorName,
            Pageable pageable
    );
}

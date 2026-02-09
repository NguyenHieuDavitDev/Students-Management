package com.example.stduents_management.traininglevel.repository;

import com.example.stduents_management.traininglevel.entity.TrainingLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TrainingLevelRepository
        extends JpaRepository<TrainingLevel, UUID> {

    boolean existsByTrainingLevelNameIgnoreCase(String name);

    boolean existsByTrainingLevelNameIgnoreCaseAndTrainingLevelIdNot(
            String name,
            UUID id
    );

    Page<TrainingLevel> findByTrainingLevelNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    java.util.Optional<TrainingLevel> findByTrainingLevelName(String trainingLevelName);
}

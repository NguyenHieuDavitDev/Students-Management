package com.example.stduents_management.position.repository;

import com.example.stduents_management.position.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PositionRepository extends JpaRepository<Position, UUID> {

    boolean existsByPositionCodeIgnoreCase(String code);

    boolean existsByPositionNameIgnoreCase(String name);

    boolean existsByPositionCodeIgnoreCaseAndPositionIdNot(
            String code, UUID id
    );

    boolean existsByPositionNameIgnoreCaseAndPositionIdNot(
            String name, UUID id
    );

    Page<Position> findByPositionCodeContainingIgnoreCaseOrPositionNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );
}

package com.example.stduents_management.building.repository;


import com.example.stduents_management.building.entity.Building;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {

    boolean existsByBuildingCode(String code);

    boolean existsByBuildingCodeAndBuildingIdNot(String code, UUID id);

    Page<Building> findByBuildingCodeContainingIgnoreCaseOrBuildingNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

    Optional<Building> findByBuildingCodeIgnoreCase(String buildingCode);
}
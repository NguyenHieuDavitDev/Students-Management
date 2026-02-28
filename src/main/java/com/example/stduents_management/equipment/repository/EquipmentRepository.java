package com.example.stduents_management.equipment.repository;

import com.example.stduents_management.equipment.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByEquipmentCode(String equipmentCode);

    boolean existsByEquipmentCodeAndEquipmentIdNot(String equipmentCode, Long id);

    Page<Equipment> findByEquipmentCodeContainingIgnoreCaseOrEquipmentNameContainingIgnoreCase(
            String equipmentCode,
            String equipmentName,
            Pageable pageable
    );
}

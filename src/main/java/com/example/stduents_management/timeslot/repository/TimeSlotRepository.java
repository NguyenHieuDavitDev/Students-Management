package com.example.stduents_management.timeslot.repository;

import com.example.stduents_management.timeslot.entity.TimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {

    boolean existsBySlotCode(String slotCode);

    boolean existsBySlotCodeAndIdNot(String slotCode, Integer id);

    Page<TimeSlot> findBySlotCodeContainingIgnoreCase(String keyword, Pageable pageable);
}

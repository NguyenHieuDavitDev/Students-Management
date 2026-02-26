package com.example.stduents_management.roomtype.repository;

import com.example.stduents_management.roomtype.entity.RoomType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomTypeRepository extends JpaRepository<RoomType, UUID> {

    boolean existsByRoomTypeCode(String code);

    boolean existsByRoomTypeCodeAndRoomTypeIdNot(String code, UUID id);

    Page<RoomType> findByRoomTypeCodeContainingIgnoreCaseOrRoomTypeNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );
}
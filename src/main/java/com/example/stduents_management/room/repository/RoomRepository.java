package com.example.stduents_management.room.repository;

import com.example.stduents_management.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomCode(String roomCode);

    Optional<Room> findByRoomCodeIgnoreCase(String roomCode);

    boolean existsByRoomCodeAndRoomIdNot(String roomCode, Long id);

    Page<Room> findByRoomCodeContainingIgnoreCaseOrRoomNameContainingIgnoreCase(
            String roomCode,
            String roomName,
            Pageable pageable
    );
}
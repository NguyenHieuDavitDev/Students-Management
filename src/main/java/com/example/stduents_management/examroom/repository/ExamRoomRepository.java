package com.example.stduents_management.examroom.repository;

import com.example.stduents_management.examroom.entity.ExamRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamRoomRepository extends JpaRepository<ExamRoom, UUID> {

    @Query("""
        SELECT er FROM ExamRoom er
        JOIN er.room r
        JOIN r.building b
        WHERE (:keyword IS NULL OR :keyword = ''
              OR LOWER(r.roomCode)   LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(r.roomName)   LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.buildingName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(er.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:from IS NULL OR er.createdAt >= :from)
          AND (:to   IS NULL OR er.createdAt <= :to)
        """)
    Page<ExamRoom> search(
        @Param("keyword") String keyword,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );

    @Query("""
        SELECT er FROM ExamRoom er
        JOIN FETCH er.room r
        JOIN FETCH r.building
        ORDER BY r.roomCode ASC
        """)
    List<ExamRoom> findAllOrdered();

    Optional<ExamRoom> findByRoom_RoomId(Long roomId);

    boolean existsByRoom_RoomId(Long roomId);

    boolean existsByRoom_RoomIdAndIdNot(Long roomId, UUID excludeId);
}

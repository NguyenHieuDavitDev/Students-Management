package com.example.stduents_management.roomblocktime.repository;

import com.example.stduents_management.roomblocktime.entity.RoomBlockTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RoomBlockTimeRepository extends JpaRepository<RoomBlockTime, UUID> {

    /**
     * Tìm kiếm gần đúng theo: mã/tên phòng, lý do, loại khóa, trạng thái.
     */
    @Query("""
        SELECT b FROM RoomBlockTime b
        LEFT JOIN b.room r
        LEFT JOIN b.timeSlot t
        WHERE (:keyword IS NULL OR :keyword = '' OR
               LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(r.roomName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(b.reason) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               (t.slotCode IS NOT NULL AND LOWER(t.slotCode) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        """)
    Page<RoomBlockTime> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

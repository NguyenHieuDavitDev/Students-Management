package com.example.stduents_management.scheduleoverride.repository;

import com.example.stduents_management.scheduleoverride.entity.ScheduleOverride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ScheduleOverrideRepository extends JpaRepository<ScheduleOverride, UUID> {

    @Query("""
            SELECT DISTINCT o FROM ScheduleOverride o
            LEFT JOIN o.schedule s
            LEFT JOIN s.semester sem
            LEFT JOIN s.classSection cs
            LEFT JOIN s.lecturer l
            LEFT JOIN s.room r
            LEFT JOIN s.timeSlot ts
            WHERE (:keyword IS NULL OR :keyword = ''
                OR LOWER(o.reason) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(sem.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(cs.classCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(cs.course.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(l.lecturerCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(l.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ScheduleOverride> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT o FROM ScheduleOverride o LEFT JOIN FETCH o.schedule s " +
           "LEFT JOIN FETCH s.semester LEFT JOIN FETCH s.classSection cs LEFT JOIN FETCH cs.course " +
           "LEFT JOIN FETCH s.lecturer LEFT JOIN FETCH s.room LEFT JOIN FETCH s.timeSlot " +
           "LEFT JOIN FETCH o.newRoom LEFT JOIN FETCH o.newTimeSlot LEFT JOIN FETCH o.newLecturer " +
           "ORDER BY o.overrideDate DESC, o.createdAt DESC")
    List<ScheduleOverride> findAllForPrint();
}

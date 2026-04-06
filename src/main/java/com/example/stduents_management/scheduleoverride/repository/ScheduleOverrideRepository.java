package com.example.stduents_management.scheduleoverride.repository;

import com.example.stduents_management.scheduleoverride.entity.OverrideType;
import com.example.stduents_management.scheduleoverride.entity.ScheduleOverride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.stduents_management.scheduleoverride.entity.OverrideStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ScheduleOverrideRepository extends JpaRepository<ScheduleOverride, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ScheduleOverride o WHERE o.schedule.id = :scheduleId")
    void deleteByScheduleId(@Param("scheduleId") UUID scheduleId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ScheduleOverride o WHERE o.schedule.semester.id = :semesterId")
    void deleteByScheduleSemesterId(@Param("semesterId") Long semesterId);

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

    /** Override đang áp dụng trong khoảng ngày (lịch tuần) — có fetch phòng / tiết mới. */
    @Query("""
            SELECT o FROM ScheduleOverride o
            JOIN FETCH o.schedule s
            LEFT JOIN FETCH s.semester
            LEFT JOIN FETCH s.classSection cs
            LEFT JOIN FETCH cs.course
            LEFT JOIN FETCH s.lecturer
            LEFT JOIN FETCH s.room
            LEFT JOIN FETCH s.timeSlot
            LEFT JOIN FETCH o.newRoom
            LEFT JOIN FETCH o.newTimeSlot
            WHERE s.semester.id = :semesterId
            AND o.status = :status
            AND (
                (o.overrideDate >= :from AND o.overrideDate <= :to)
                OR (o.movedToDate IS NOT NULL AND o.movedToDate >= :from AND o.movedToDate <= :to)
            )
            """)
    List<ScheduleOverride> findActiveForSemesterAndDateRange(
            @Param("semesterId") Long semesterId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") OverrideStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM ScheduleOverride o
            WHERE o.schedule.id = :scheduleId
            AND o.overrideDate = :overrideDate
            AND o.status = :status
            AND o.overrideType IN :types
            """)
    void deleteByScheduleOverrideDateTypesAndStatus(
            @Param("scheduleId") UUID scheduleId,
            @Param("overrideDate") LocalDate overrideDate,
            @Param("types") Collection<OverrideType> types,
            @Param("status") OverrideStatus status
    );
}

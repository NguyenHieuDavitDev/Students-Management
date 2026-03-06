package com.example.stduents_management.schedule.repository;

import com.example.stduents_management.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    List<Schedule> findBySemester_Id(Long semesterId);

    @Query("""
            SELECT s FROM Schedule s
            WHERE (:keyword IS NULL OR :keyword = ''
                OR LOWER(s.semester.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.semester.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.classSection.classCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.classSection.className) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.classSection.course.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.classSection.course.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.lecturer.lecturerCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.lecturer.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.room.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.room.roomName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(s.timeSlot.slotCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Schedule> search(@Param("keyword") String keyword, Pageable pageable);
}

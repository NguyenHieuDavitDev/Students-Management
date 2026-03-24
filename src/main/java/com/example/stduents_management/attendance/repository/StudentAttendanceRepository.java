package com.example.stduents_management.attendance.repository;

import com.example.stduents_management.attendance.entity.StudentAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentAttendanceRepository extends JpaRepository<StudentAttendance, UUID> {

    boolean existsByStudent_StudentIdAndCourseClass_IdAndAttendanceDate(
            UUID studentId,
            Long courseClassId,
            LocalDate attendanceDate
    );

    Optional<StudentAttendance> findByStudent_StudentIdAndCourseClass_IdAndAttendanceDate(
            UUID studentId,
            Long courseClassId,
            LocalDate attendanceDate
    );

    @Query("""
        SELECT a
        FROM StudentAttendance a
        JOIN a.student s
        JOIN a.courseClass cs
        JOIN cs.course c
        LEFT JOIN a.markedBy l
        WHERE (:courseClassId IS NULL OR cs.id = :courseClassId)
          AND (:attendanceDate IS NULL OR a.attendanceDate = :attendanceDate)
          AND (
            :keyword IS NULL OR :keyword = '' OR
            LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(cs.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(cs.className) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(l.lecturerCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(l.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(a.note) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
    Page<StudentAttendance> search(
            @Param("keyword") String keyword,
            @Param("courseClassId") Long courseClassId,
            @Param("attendanceDate") LocalDate attendanceDate,
            Pageable pageable
    );

    @Query("""
        SELECT a
        FROM StudentAttendance a
        JOIN FETCH a.student s
        JOIN FETCH a.courseClass cs
        JOIN FETCH cs.course c
        LEFT JOIN FETCH a.markedBy l
        WHERE cs.id = :courseClassId
          AND a.attendanceDate = :attendanceDate
        ORDER BY s.fullName ASC
    """)
    List<StudentAttendance> findAllByCourseClassIdAndAttendanceDate(
            @Param("courseClassId") Long courseClassId,
            @Param("attendanceDate") LocalDate attendanceDate
    );
}


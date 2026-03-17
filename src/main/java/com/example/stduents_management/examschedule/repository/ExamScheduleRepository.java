package com.example.stduents_management.examschedule.repository;

import com.example.stduents_management.examschedule.entity.ExamSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, UUID> {

    @Query("""
        SELECT es FROM ExamSchedule es
        JOIN es.classSection cs
        JOIN cs.course c
        JOIN cs.semester sem
        JOIN es.examType et
        WHERE (:keyword IS NULL OR :keyword = ''
              OR LOWER(c.courseCode)   LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.courseName)   LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(cs.classCode)   LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(sem.name)       LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(et.name)        LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:fromDate IS NULL OR es.examDate >= :fromDate)
          AND (:toDate   IS NULL OR es.examDate <= :toDate)
        """)
    Page<ExamSchedule> search(
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("""
        SELECT es FROM ExamSchedule es
        JOIN FETCH es.classSection cs
        JOIN FETCH cs.course c
        JOIN FETCH cs.semester sem
        JOIN FETCH es.examType et
        ORDER BY sem.startDate, c.courseCode, et.name
        """)
    List<ExamSchedule> findAllOrdered();
}
package com.example.stduents_management.studenttuition.repository;

import com.example.stduents_management.studenttuition.entity.StudentTuition;
import com.example.stduents_management.studenttuition.entity.StudentTuitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StudentTuitionRepository extends JpaRepository<StudentTuition, UUID> {

    @Query("""
            SELECT st
            FROM StudentTuition st
            JOIN st.student s
            JOIN st.semester sem
            WHERE (:status IS NULL OR st.status = :status)
              AND (
                    :keyword IS NULL
                 OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(s.fullName)   LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(sem.code)     LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(sem.name)     LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<StudentTuition> search(
            @Param("keyword") String keyword,
            @Param("status") StudentTuitionStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT st
            FROM StudentTuition st
            JOIN st.student s
            JOIN st.semester sem
            ORDER BY sem.academicYear DESC, sem.term DESC, s.studentCode ASC
            """)
    List<StudentTuition> findAllOrdered();
}


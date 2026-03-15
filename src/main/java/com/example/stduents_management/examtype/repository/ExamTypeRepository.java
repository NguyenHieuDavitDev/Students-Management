package com.example.stduents_management.examtype.repository;

import com.example.stduents_management.examtype.entity.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ExamTypeRepository extends JpaRepository<ExamType, UUID> {


    @Query("""
        SELECT e FROM ExamType e
        WHERE (:keyword IS NULL OR :keyword = ''
              OR LOWER(e.name)        LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:from IS NULL OR e.createdAt >= :from)
          AND (:to   IS NULL OR e.createdAt <= :to)
        """)
    Page<ExamType> search(
            @Param("keyword") String keyword,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM ExamType e
        ORDER BY e.createdAt DESC, e.name ASC
        """)
    List<ExamType> findAllOrdered();
}

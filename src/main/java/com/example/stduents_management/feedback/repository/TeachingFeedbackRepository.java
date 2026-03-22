package com.example.stduents_management.feedback.repository;

import com.example.stduents_management.feedback.entity.TeachingFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TeachingFeedbackRepository extends JpaRepository<TeachingFeedback, UUID> {

    @Query("""
            SELECT f FROM TeachingFeedback f
            JOIN f.student s
            JOIN f.lecturer l
            JOIN f.subject c
            WHERE (:from IS NULL OR f.createdAt >= :from)
              AND (:to IS NULL OR f.createdAt <= :to)
              AND (
                   :keyword IS NULL OR :keyword = ''
                   OR LOWER(f.comment) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(l.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(l.lecturerCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR (:ratingMatch IS NOT NULL AND f.rating = :ratingMatch)
              )
            """)
    Page<TeachingFeedback> search(
            @Param("keyword") String keyword,
            @Param("ratingMatch") Integer ratingMatch,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
            SELECT f FROM TeachingFeedback f
            JOIN FETCH f.student s
            JOIN FETCH f.lecturer l
            JOIN FETCH f.subject c
            ORDER BY f.createdAt DESC
            """)
    List<TeachingFeedback> findAllForExport();
}

package com.example.stduents_management.gradescale.repository;

import com.example.stduents_management.gradescale.entity.GradeScale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface GradeScaleRepository extends JpaRepository<GradeScale, UUID> {

    @Query("""
        SELECT g FROM GradeScale g
        WHERE (:keyword IS NULL OR :keyword = '' OR
               LOWER(g.letterGrade) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<GradeScale> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /** Tìm mức thang điểm áp dụng cho điểm số cho trước. */
    @Query("""
        SELECT g FROM GradeScale g
        WHERE :score >= g.minScore AND :score <= g.maxScore
        ORDER BY g.minScore DESC
        """)
    Optional<GradeScale> findByScore(@Param("score") BigDecimal score);

    boolean existsByLetterGrade(String letterGrade);

    boolean existsByLetterGradeAndIdNot(String letterGrade, UUID id);
}

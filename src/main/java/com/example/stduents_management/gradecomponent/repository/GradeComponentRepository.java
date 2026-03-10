package com.example.stduents_management.gradecomponent.repository;

import com.example.stduents_management.gradecomponent.entity.GradeComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeComponentRepository extends JpaRepository<GradeComponent, UUID> {

    List<GradeComponent> findByClassSection_IdOrderByComponentName(Long classSectionId);

    Optional<GradeComponent> findByClassSection_IdAndComponentNameIgnoreCase(Long classSectionId, String componentName);

    @Query("""
        SELECT g FROM GradeComponent g
        LEFT JOIN g.classSection cs
        LEFT JOIN cs.course c
        WHERE (:keyword IS NULL OR :keyword = '' OR
               LOWER(g.componentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(cs.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(cs.className) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(c.courseName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<GradeComponent> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

package com.example.stduents_management.graduationcondition.repository;

import com.example.stduents_management.graduationcondition.entity.GraduationCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GraduationConditionRepository extends JpaRepository<GraduationCondition, Long> {

    @Query("""
        SELECT g FROM GraduationCondition g
        JOIN g.program p
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(p.programCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.programName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(g.requiredCertificate) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.programName ASC, g.createdAt DESC
        """)
    Page<GraduationCondition> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT g FROM GraduationCondition g
        JOIN FETCH g.program p
        LEFT JOIN FETCH p.major
        ORDER BY p.programName ASC, g.createdAt DESC
        """)
    List<GraduationCondition> findAllOrdered();

    Optional<GraduationCondition> findByProgram_ProgramId(UUID programId);

    boolean existsByProgram_ProgramIdAndIdNot(UUID programId, Long excludeId);
}

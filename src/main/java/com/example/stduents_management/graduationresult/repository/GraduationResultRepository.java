package com.example.stduents_management.graduationresult.repository;

import com.example.stduents_management.graduationresult.entity.GraduationResult;
import com.example.stduents_management.graduationresult.entity.GraduationResultStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GraduationResultRepository extends JpaRepository<GraduationResult, Long> {

    @Query("""
        SELECT r FROM GraduationResult r
        JOIN r.student s
        JOIN r.program p
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.programCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.programName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR r.status = :status)
        ORDER BY r.checkedAt DESC, s.studentCode ASC
        """)
    Page<GraduationResult> search(
            @Param("keyword") String keyword,
            @Param("status") GraduationResultStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT r FROM GraduationResult r
        JOIN FETCH r.student s
        JOIN FETCH r.program p
        LEFT JOIN FETCH p.major
        ORDER BY r.checkedAt DESC, s.studentCode ASC
        """)
    List<GraduationResult> findAllOrdered();

    Optional<GraduationResult> findByStudent_StudentIdAndProgram_ProgramId(UUID studentId, UUID programId);

    boolean existsByStudent_StudentIdAndProgram_ProgramIdAndIdNot(UUID studentId, UUID programId, Long excludeId);
}


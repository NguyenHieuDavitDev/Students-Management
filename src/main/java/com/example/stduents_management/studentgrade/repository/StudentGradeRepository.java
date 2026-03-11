package com.example.stduents_management.studentgrade.repository;

import com.example.stduents_management.studentgrade.entity.StudentGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentGradeRepository extends JpaRepository<StudentGrade, UUID> {

    /** Tất cả điểm của một sinh viên trong một lớp học phần (dùng tính tổng kết). */
    List<StudentGrade> findAllByStudent_StudentIdAndCourseClass_Id(UUID studentId, Long courseClassId);

    /** Tất cả điểm của mọi sinh viên trong một lớp (batch load cho class transcript). */
    List<StudentGrade> findAllByCourseClass_Id(Long courseClassId);

    Optional<StudentGrade> findByStudent_StudentIdAndCourseClass_IdAndGradeComponent_Id(
            UUID studentId, Long courseClassId, UUID gradeComponentId);

    boolean existsByStudent_StudentIdAndCourseClass_IdAndGradeComponent_Id(
            UUID studentId, Long courseClassId, UUID gradeComponentId);

    @Query("""
        SELECT sg FROM StudentGrade sg
        LEFT JOIN sg.student s
        LEFT JOIN sg.courseClass cc
        LEFT JOIN cc.course c
        LEFT JOIN sg.gradeComponent gc
        WHERE (:keyword IS NULL OR :keyword = '' OR
               LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(cc.classCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(gc.componentName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<StudentGrade> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        SELECT sg FROM StudentGrade sg
        LEFT JOIN sg.student s
        LEFT JOIN sg.courseClass cc
        LEFT JOIN sg.gradeComponent gc
        WHERE (:courseClassId IS NULL OR cc.id = :courseClassId)
        AND (:keyword IS NULL OR :keyword = '' OR
             LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<StudentGrade> searchByCourseClassAndKeyword(
            @Param("courseClassId") Long courseClassId,
            @Param("keyword") String keyword,
            Pageable pageable);
}

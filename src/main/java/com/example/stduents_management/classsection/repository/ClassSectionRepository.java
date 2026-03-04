package com.example.stduents_management.classsection.repository;

import com.example.stduents_management.classsection.entity.ClassSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {

    boolean existsByClassCode(String classCode);

    boolean existsByClassCodeAndIdNot(String classCode, Long id);

    @Query("""
           select cs from ClassSection cs
           where (:keyword is null or :keyword = ''
              or lower(cs.classCode) like lower(concat('%', :keyword, '%'))
              or lower(cs.className) like lower(concat('%', :keyword, '%'))
              or lower(cs.course.courseCode) like lower(concat('%', :keyword, '%'))
              or lower(cs.course.courseName) like lower(concat('%', :keyword, '%'))
              or lower(cs.semester.code) like lower(concat('%', :keyword, '%')))
           """)
    Page<ClassSection> searchByClassCodeOrClassNameOrCourseOrSemester(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    Optional<ClassSection> findByClassCodeIgnoreCase(String classCode);
}

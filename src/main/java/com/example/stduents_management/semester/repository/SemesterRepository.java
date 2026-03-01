package com.example.stduents_management.semester.repository;

import com.example.stduents_management.semester.entity.Semester;
import com.example.stduents_management.semester.entity.SemesterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("""
           select s from Semester s
           where (:keyword is null or :keyword = ''
              or lower(s.code) like lower(concat('%', :keyword, '%'))
              or lower(s.name) like lower(concat('%', :keyword, '%')))
           """)
    Page<Semester> searchByCodeOrName(@Param("keyword") String keyword, Pageable pageable);

    List<Semester> findByStatus(SemesterStatus status);
}
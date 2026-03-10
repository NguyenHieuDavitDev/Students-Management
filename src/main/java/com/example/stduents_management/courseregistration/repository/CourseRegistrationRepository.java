package com.example.stduents_management.courseregistration.repository;

import com.example.stduents_management.courseregistration.entity.CourseRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, Long> {

    boolean existsByStudent_StudentIdAndClassSection_Id(UUID studentId, Long classSectionId);

    List<CourseRegistration> findByClassSection_IdOrderByStudent_FullName(Long classSectionId);

    long countByClassSection_Id(Long classSectionId);

    @Query("""
            select cr from CourseRegistration cr
            where (:keyword is null or :keyword = ''
                or lower(cr.student.studentCode) like lower(concat('%', :keyword, '%'))
                or lower(cr.student.fullName) like lower(concat('%', :keyword, '%'))
                or lower(cr.classSection.classCode) like lower(concat('%', :keyword, '%'))
                or lower(cr.classSection.course.courseCode) like lower(concat('%', :keyword, '%'))
                or lower(cr.classSection.course.courseName) like lower(concat('%', :keyword, '%'))
                or lower(cr.classSection.semester.code) like lower(concat('%', :keyword, '%'))
            )
            """)
    Page<CourseRegistration> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}


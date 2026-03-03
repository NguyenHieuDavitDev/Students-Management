package com.example.stduents_management.lecturercourseclass.repository;

import com.example.stduents_management.lecturercourseclass.entity.LecturerCourseClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LecturerCourseClassRepository extends JpaRepository<LecturerCourseClass, Long> {

    boolean existsByClassSection_IdAndLecturer_LecturerId(Long classSectionId, java.util.UUID lecturerId);

    boolean existsByClassSection_IdAndLecturer_LecturerIdAndIdNot(Long classSectionId, java.util.UUID lecturerId, Long id);

    @Query("""
           select lcc from LecturerCourseClass lcc
           where (:keyword is null or :keyword = ''
              or lower(lcc.classSection.classCode) like lower(concat('%', :keyword, '%'))
              or lower(lcc.classSection.className) like lower(concat('%', :keyword, '%'))
              or lower(lcc.classSection.course.courseCode) like lower(concat('%', :keyword, '%'))
              or lower(lcc.classSection.course.courseName) like lower(concat('%', :keyword, '%'))
              or lower(lcc.classSection.semester.code) like lower(concat('%', :keyword, '%'))
              or lower(lcc.lecturer.lecturerCode) like lower(concat('%', :keyword, '%'))
              or lower(lcc.lecturer.fullName) like lower(concat('%', :keyword, '%'))
           )
           """)
    Page<LecturerCourseClass> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}


package com.example.stduents_management.lecturercourseclass.repository;

import com.example.stduents_management.lecturercourseclass.entity.LecturerCourseClass;
import com.example.stduents_management.lecturer.entity.Lecturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LecturerCourseClassRepository extends JpaRepository<LecturerCourseClass, Long> {

    List<LecturerCourseClass> findByClassSection_Semester_IdOrderByClassSection_ClassCode(Long semesterId);

    boolean existsByClassSection_IdAndLecturer_LecturerId(Long classSectionId, java.util.UUID lecturerId);

    boolean existsByClassSection_IdAndLecturer_LecturerIdAndIdNot(Long classSectionId, java.util.UUID lecturerId, Long id);

    @Query("""
           SELECT DISTINCT lcc.lecturer
           FROM LecturerCourseClass lcc
           WHERE lcc.classSection.id = :classSectionId
           ORDER BY lcc.lecturer.fullName ASC
           """)
    List<Lecturer> findLecturersByClassSectionId(@Param("classSectionId") Long classSectionId);

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


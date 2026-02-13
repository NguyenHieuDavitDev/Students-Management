package com.example.stduents_management.course.repository;

import com.example.stduents_management.course.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    boolean existsByCourseCodeIgnoreCaseAndIdNot(String courseCode, UUID id);

    Page<Course> findByCourseCodeContainingIgnoreCaseOrCourseNameContainingIgnoreCaseOrFaculty_FacultyNameContainingIgnoreCase(
            String code,
            String name,
            String facultyName,
            Pageable pageable
    );
}
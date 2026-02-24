package com.example.stduents_management.courseprerequisite.repository;

import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.courseprerequisite.entity.CoursePrerequisite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CoursePrerequisiteRepository extends JpaRepository<CoursePrerequisite, UUID> {

    List<CoursePrerequisite> findByCourse(Course course);

    void deleteByCourse(Course course);

    boolean existsByCourseAndPrerequisiteCourse(Course course, Course prerequisiteCourse);

    Page<CoursePrerequisite> findByCourse_CourseCodeContainingIgnoreCaseOrCourse_CourseNameContainingIgnoreCaseOrPrerequisiteCourse_CourseCodeContainingIgnoreCaseOrPrerequisiteCourse_CourseNameContainingIgnoreCase(
            String courseCode,
            String courseName,
            String preCourseCode,
            String preCourseName,
            Pageable pageable
    );
}



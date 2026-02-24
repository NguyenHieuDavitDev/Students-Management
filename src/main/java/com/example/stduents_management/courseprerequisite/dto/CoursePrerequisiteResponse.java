package com.example.stduents_management.courseprerequisite.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePrerequisiteResponse {

    private UUID id;

    private UUID courseId;
    private String courseCode;
    private String courseName;

    private UUID prerequisiteCourseId;
    private String prerequisiteCourseCode;
    private String prerequisiteCourseName;
}


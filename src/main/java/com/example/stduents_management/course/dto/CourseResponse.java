package com.example.stduents_management.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseResponse {

    private UUID id;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private Integer lectureHours;
    private Integer practiceHours;
    private UUID facultyId;
    private String facultyName;
    private String description;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.example.stduents_management.courseprerequisite.entity;

import com.example.stduents_management.course.entity.Course;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "course_prerequisites",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"course_id", "prerequisite_course_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoursePrerequisite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prerequisite_course_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Course prerequisiteCourse;
}


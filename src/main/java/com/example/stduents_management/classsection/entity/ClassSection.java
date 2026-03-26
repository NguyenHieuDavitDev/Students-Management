package com.example.stduents_management.classsection.entity;

import com.example.stduents_management.classroom.entity.ClassEntity;
import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.room.entity.Room;
import com.example.stduents_management.semester.entity.Semester;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "class_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "class_code", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String classCode;

    @Column(name = "class_name", nullable = false, columnDefinition = "NVARCHAR(200)")
    private String className;

    /**
     * Lớp hành chính (niên khóa) mà lớp học phần này thuộc về — dùng để đồng bộ phân công GV với “lớp” trong QLĐT.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrative_class_id")
    private ClassEntity administrativeClass;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @Column(name = "current_students", nullable = false)
    private Integer currentStudents;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ClassSectionStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "note", columnDefinition = "NVARCHAR(500)")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.currentStudents == null) {
            this.currentStudents = 0;
        }
        if (this.status == null) {
            this.status = ClassSectionStatus.OPEN;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

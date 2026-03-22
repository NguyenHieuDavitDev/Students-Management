package com.example.stduents_management.document.entity;

import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id", columnDefinition = "uniqueidentifier")
    private UUID documentId;

    @Column(name = "title", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "file_url", nullable = false, length = 500, columnDefinition = "NVARCHAR(500)")
    private String fileUrl;

    @Column(name = "file_type", length = 50, columnDefinition = "NVARCHAR(50)")
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, columnDefinition = "uniqueidentifier")
    private Course subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false, columnDefinition = "uniqueidentifier")
    private User uploadedBy;

    @Column(name = "description", length = 500, columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}

package com.example.stduents_management.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private UUID documentId;
    private String title;
    private String fileUrl;
    private String fileType;
    private UUID subjectId;
    private String subjectCourseCode;
    private String subjectCourseName;
    private UUID uploadedById;
    private String uploadedByUsername;
    private String uploadedByEmail;
    private String description;
    private LocalDateTime createdAt;
}

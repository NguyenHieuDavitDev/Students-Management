package com.example.stduents_management.document.repository;

import com.example.stduents_management.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Page<Document> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrFileTypeContainingIgnoreCaseOrFileUrlContainingIgnoreCase(
            String title,
            String description,
            String fileType,
            String fileUrl,
            Pageable pageable
    );
}

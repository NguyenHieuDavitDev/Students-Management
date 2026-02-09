package com.example.stduents_management.common.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/avatars/";

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + filename);

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/avatars/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Không thể upload ảnh", e);
        }
    }
}

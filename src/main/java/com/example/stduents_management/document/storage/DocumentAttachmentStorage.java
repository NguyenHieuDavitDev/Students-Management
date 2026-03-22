package com.example.stduents_management.document.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class DocumentAttachmentStorage {

    public static final String PUBLIC_PREFIX = "/uploads/documents/";

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "scr", "pif", "msi", "dll", "sys", "vbs", "wsf", "msc"
    );

    private final Path rootDirectory;
    private final long maxBytes;

    public DocumentAttachmentStorage(
            @Value("${app.document-upload.max-bytes:52428800}") long maxBytes
    ) {
        this.rootDirectory = Path.of("uploads", "documents").toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    @PostConstruct
    public void ensureDirectory() throws IOException {
        Files.createDirectories(rootDirectory);
    }

    public boolean isManagedUrl(String fileUrl) {
        return fileUrl != null && fileUrl.startsWith(PUBLIC_PREFIX);
    }

    /**
     * Lưu file, trả về URL công khai (bắt đầu bằng {@link #PUBLIC_PREFIX}).
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File tải lên rỗng");
        }

        if (file.getSize() > maxBytes) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Dung lượng file vượt quá giới hạn cho phép (" + (maxBytes / 1024 / 1024) + " MB)"
            );
        }

        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            original = "file";
        }
        original = StringUtils.cleanPath(original);
        if (original.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên file không hợp lệ");
        }

        String ext = extensionOf(original);
        if (ext != null && BLOCKED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Định dạng ." + ext + " không được phép tải lên"
            );
        }

        String safeBase = sanitizeBaseName(stripExtension(original));
        String suffix = ext != null && !ext.isEmpty() ? "." + ext.toLowerCase(Locale.ROOT) : "";
        String storedName = UUID.randomUUID() + "_" + safeBase + suffix;
        Path target = rootDirectory.resolve(storedName).normalize();
        if (!target.startsWith(rootDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Đường dẫn lưu file không hợp lệ");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không lưu được file: " + e.getMessage());
        }

        return PUBLIC_PREFIX + storedName;
    }

    public void deleteIfStored(String fileUrl) {
        if (!isManagedUrl(fileUrl)) {
            return;
        }
        String name = fileUrl.substring(PUBLIC_PREFIX.length());
        if (name.isEmpty() || name.contains("..") || name.contains("/") || name.contains("\\")) {
            return;
        }
        Path target = rootDirectory.resolve(name).normalize();
        if (!target.startsWith(rootDirectory)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // best-effort
        }
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return null;
        }
        return filename.substring(dot + 1);
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0) {
            return filename;
        }
        return filename.substring(0, dot);
    }

    private static String sanitizeBaseName(String base) {
        String s = base.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (s.isBlank()) {
            s = "file";
        }
        if (s.length() > 120) {
            s = s.substring(0, 120);
        }
        return s;
    }
}

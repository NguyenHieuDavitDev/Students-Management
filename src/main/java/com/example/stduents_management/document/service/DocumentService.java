package com.example.stduents_management.document.service;

import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.document.dto.DocumentRequest;
import com.example.stduents_management.document.dto.DocumentResponse;
import com.example.stduents_management.document.entity.Document;
import com.example.stduents_management.document.repository.DocumentRepository;
import com.example.stduents_management.document.storage.DocumentAttachmentStorage;
import com.example.stduents_management.user.entity.User;
import com.example.stduents_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private static final Pattern URL_EXT = Pattern.compile("\\.([a-zA-Z0-9]{1,20})(?:\\?.*)?$");

    private final DocumentRepository documentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final DocumentAttachmentStorage attachmentStorage;

    @Transactional(readOnly = true)
    public Page<DocumentResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Document> data =
                (keyword == null || keyword.isBlank())
                        ? documentRepository.findAll(pageable)
                        : documentRepository
                        .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrFileTypeContainingIgnoreCaseOrFileUrlContainingIgnoreCase(
                                keyword, keyword, keyword, keyword, pageable
                        );

        return data.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(UUID id) {
        return documentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài liệu"));
    }

    @Transactional
    public DocumentResponse create(DocumentRequest req) {
        Document doc = new Document();
        apply(doc, req);
        return toResponse(documentRepository.save(doc));
    }

    @Transactional
    public DocumentResponse update(UUID id, DocumentRequest req) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài liệu"));
        apply(doc, req);
        return toResponse(documentRepository.save(doc));
    }

    @Transactional
    public void delete(UUID id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài liệu"));
        attachmentStorage.deleteIfStored(doc.getFileUrl());
        documentRepository.delete(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getForPrint() {
        return documentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void importExcel(MultipartFile file) throws Exception {
        DataFormatter formatter = new DataFormatter(Locale.getDefault());

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                final int rowNum = i + 1;
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String title = readString(row, 0, formatter);
                if (title == null || title.isBlank()) continue;

                String fileUrl = readString(row, 1, formatter);
                if (fileUrl == null || fileUrl.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": fileUrl không được để trống"
                    );
                }

                String fileType = readString(row, 2, formatter);
                String courseCode = readString(row, 3, formatter);
                String username = readString(row, 4, formatter);
                String description = readString(row, 5, formatter);

                if (courseCode == null || courseCode.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": courseCode (mã môn) không được để trống"
                    );
                }

                if (username == null || username.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Dòng " + rowNum + ": username người tải không được để trống"
                    );
                }

                Course subject = courseRepository.findByCourseCodeIgnoreCase(courseCode)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Dòng " + rowNum + ": Không tìm thấy môn học với mã " + courseCode
                                ));

                User uploader = userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Dòng " + rowNum + ": Không tìm thấy user với username " + username
                                ));

                Document doc = new Document();
                doc.setTitle(title.trim());
                doc.setFileUrl(fileUrl.trim());
                doc.setFileType(fileType != null ? fileType.trim() : null);
                doc.setSubject(subject);
                doc.setUploadedBy(uploader);
                doc.setDescription(description != null ? description.trim() : null);

                documentRepository.save(doc);
            }
        }
    }

    @Transactional(readOnly = true)
    public byte[] exportExcel() throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Documents");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Document ID");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("File URL");
            header.createCell(3).setCellValue("File Type");
            header.createCell(4).setCellValue("Course Code");
            header.createCell(5).setCellValue("Uploaded By Username");
            header.createCell(6).setCellValue("Description");
            header.createCell(7).setCellValue("Created At");

            List<Document> list = documentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

            int rowNum = 1;
            for (Document d : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(d.getDocumentId() != null ? d.getDocumentId().toString() : "");
                row.createCell(1).setCellValue(d.getTitle());
                row.createCell(2).setCellValue(d.getFileUrl());
                row.createCell(3).setCellValue(d.getFileType() != null ? d.getFileType() : "");
                row.createCell(4).setCellValue(
                        d.getSubject() != null ? d.getSubject().getCourseCode() : "");
                row.createCell(5).setCellValue(
                        d.getUploadedBy() != null ? d.getUploadedBy().getUsername() : "");
                row.createCell(6).setCellValue(d.getDescription() != null ? d.getDescription() : "");
                row.createCell(7).setCellValue(d.getCreatedAt() != null ? d.getCreatedAt().toString() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void apply(Document doc, DocumentRequest req) {
        Course subject = courseRepository.findById(req.getSubjectId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Môn học không tồn tại"));

        User uploader = userRepository.findById(req.getUploadedById())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        doc.setTitle(req.getTitle().trim());
        applyFileFields(doc, req);
        doc.setSubject(subject);
        doc.setUploadedBy(uploader);
        doc.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
    }

    private void applyFileFields(Document doc, DocumentRequest req) {
        MultipartFile upload = req.getAttachmentFile();
        String previousUrl = doc.getFileUrl();

        if (upload != null && !upload.isEmpty()) {
            if (attachmentStorage.isManagedUrl(previousUrl)) {
                attachmentStorage.deleteIfStored(previousUrl);
            }
            doc.setFileUrl(attachmentStorage.store(upload));
            doc.setFileType(resolveUploadedFileType(upload, req));
            return;
        }

        String newUrl = req.getFileUrl() != null ? req.getFileUrl().trim() : "";
        if (!StringUtils.hasText(newUrl)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cần URL file hoặc file đính kèm"
            );
        }
        if (attachmentStorage.isManagedUrl(previousUrl) && !previousUrl.equals(newUrl)) {
            attachmentStorage.deleteIfStored(previousUrl);
        }
        doc.setFileUrl(newUrl);
        if (StringUtils.hasText(req.getFileType())) {
            doc.setFileType(req.getFileType().trim());
        } else {
            doc.setFileType(extensionFromUrl(newUrl));
        }
    }

    private String resolveUploadedFileType(MultipartFile file, DocumentRequest req) {
        if (StringUtils.hasText(req.getFileType())) {
            return req.getFileType().trim();
        }
        String name = file.getOriginalFilename();
        if (!StringUtils.hasText(name)) {
            return null;
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return null;
        }
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String extensionFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String path = url;
        int q = path.indexOf('?');
        if (q >= 0) {
            path = path.substring(0, q);
        }
        Matcher m = URL_EXT.matcher(path);
        if (m.find()) {
            return m.group(1).toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private DocumentResponse toResponse(Document d) {
        Course s = d.getSubject();
        User u = d.getUploadedBy();

        return new DocumentResponse(
                d.getDocumentId(),
                d.getTitle(),
                d.getFileUrl(),
                d.getFileType(),
                s != null ? s.getId() : null,
                s != null ? s.getCourseCode() : null,
                s != null ? s.getCourseName() : null,
                u != null ? u.getId() : null,
                u != null ? u.getUsername() : null,
                u != null ? u.getEmail() : null,
                d.getDescription(),
                d.getCreatedAt()
        );
    }

    private String readString(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }
}

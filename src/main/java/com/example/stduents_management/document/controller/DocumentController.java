package com.example.stduents_management.document.controller;

import com.example.stduents_management.document.dto.DocumentRequest;
import com.example.stduents_management.document.dto.DocumentResponse;
import com.example.stduents_management.document.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(documentService.search(keyword, page, size));
    }

    @GetMapping("/print")
    public ResponseEntity<List<DocumentResponse>> print() {
        return ResponseEntity.ok(documentService.getForPrint());
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = documentService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documents.xlsx");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentResponse> createJson(@Valid @RequestBody DocumentRequest request) {
        DocumentResponse body = documentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> createMultipart(@Valid @ModelAttribute DocumentRequest request) {
        DocumentResponse body = documentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        documentService.importExcel(file);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentResponse> updateJson(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentRequest request
    ) {
        return ResponseEntity.ok(documentService.update(id, request));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> updateMultipart(
            @PathVariable UUID id,
            @Valid @ModelAttribute DocumentRequest request
    ) {
        return ResponseEntity.ok(documentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

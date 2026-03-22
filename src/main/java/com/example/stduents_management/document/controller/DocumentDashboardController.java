package com.example.stduents_management.document.controller;

import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.document.dto.DocumentRequest;
import com.example.stduents_management.document.dto.DocumentResponse;
import com.example.stduents_management.document.service.DocumentService;
import com.example.stduents_management.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/documents")
@RequiredArgsConstructor
public class DocumentDashboardController {

    private final DocumentService documentService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<DocumentResponse> documents = documentService.search(keyword, page, size);

        model.addAttribute("documents", documents);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "documents/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("documentRequest", new DocumentRequest());
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
        model.addAttribute("users", userRepository.findAll(Sort.by("username")));
        return "documents/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("documentRequest") DocumentRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
            model.addAttribute("users", userRepository.findAll(Sort.by("username")));
            return "documents/form";
        }

        try {
            documentService.create(req);
            redirect.addFlashAttribute("success", "Thêm tài liệu thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
            model.addAttribute("users", userRepository.findAll(Sort.by("username")));
            model.addAttribute("globalError", e.getMessage());
            return "documents/form";
        }

        return "redirect:/admin/documents";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        DocumentResponse d = documentService.getById(id);

        DocumentRequest req = new DocumentRequest();
        req.setTitle(d.getTitle());
        req.setFileUrl(d.getFileUrl());
        req.setFileType(d.getFileType());
        req.setSubjectId(d.getSubjectId());
        req.setUploadedById(d.getUploadedById());
        req.setDescription(d.getDescription());

        model.addAttribute("mode", "edit");
        model.addAttribute("documentId", id);
        model.addAttribute("documentRequest", req);
        model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
        model.addAttribute("users", userRepository.findAll(Sort.by("username")));
        return "documents/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("documentRequest") DocumentRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("documentId", id);
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
            model.addAttribute("users", userRepository.findAll(Sort.by("username")));
            return "documents/form";
        }

        try {
            documentService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật tài liệu thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("documentId", id);
            model.addAttribute("courses", courseRepository.findAll(Sort.by("courseCode")));
            model.addAttribute("users", userRepository.findAll(Sort.by("username")));
            model.addAttribute("globalError", e.getMessage());
            return "documents/form";
        }

        return "redirect:/admin/documents";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        documentService.delete(id);
        redirect.addFlashAttribute("success", "Xóa tài liệu thành công");
        return "redirect:/admin/documents";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("documents", documentService.getForPrint());
        return "documents/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            documentService.importExcel(file);
            redirect.addFlashAttribute("success", "Import tài liệu thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/documents";
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
}

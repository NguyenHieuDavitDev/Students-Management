package com.example.stduents_management.gradecomponent.controller;

import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.gradecomponent.dto.GradeComponentRequest;
import com.example.stduents_management.gradecomponent.dto.GradeComponentResponse;
import com.example.stduents_management.gradecomponent.service.GradeComponentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/admin/grade-components")
@RequiredArgsConstructor
public class GradeComponentDashboardController {

    private final GradeComponentService gradeComponentService;
    private final ClassSectionRepository classSectionRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<GradeComponentResponse> items = gradeComponentService.search(keyword, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "grade-components/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("gradeComponentRequest", new GradeComponentRequest());
        loadSelectData(model);
        return "grade-components/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("gradeComponentRequest") GradeComponentRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            return "grade-components/form";
        }
        gradeComponentService.create(req);
        redirect.addFlashAttribute("success", "Thêm thành phần điểm thành công");
        return "redirect:/admin/grade-components";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        GradeComponentResponse r = gradeComponentService.getById(id);
        GradeComponentRequest req = new GradeComponentRequest();
        req.setCourseClassId(r.courseClassId());
        req.setComponentName(r.componentName());
        req.setWeight(r.weight());
        req.setMaxScore(r.maxScore());
        model.addAttribute("mode", "edit");
        model.addAttribute("componentId", id);
        model.addAttribute("gradeComponentRequest", req);
        loadSelectData(model);
        return "grade-components/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("gradeComponentRequest") GradeComponentRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("componentId", id);
            loadSelectData(model);
            return "grade-components/form";
        }
        gradeComponentService.update(id, req);
        redirect.addFlashAttribute("success", "Cập nhật thành phần điểm thành công");
        return "redirect:/admin/grade-components";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        gradeComponentService.delete(id);
        redirect.addFlashAttribute("success", "Xóa thành phần điểm thành công");
        return "redirect:/admin/grade-components";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("items", gradeComponentService.getForPrint());
        return "grade-components/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            gradeComponentService.importExcel(file);
            redirect.addFlashAttribute("success", "Import thành phần điểm thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/grade-components";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = gradeComponentService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grade-components.xlsx");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    private void loadSelectData(Model model) {
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
    }
}

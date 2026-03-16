package com.example.stduents_management.examroom.controller;

import com.example.stduents_management.examroom.dto.ExamRoomRequest;
import com.example.stduents_management.examroom.dto.ExamRoomResponse;
import com.example.stduents_management.examroom.service.ExamRoomService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/exam-rooms")
@RequiredArgsConstructor
public class ExamRoomDashboardController {

    private final ExamRoomService examRoomService;

    @GetMapping
    public String index(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Model model
    ) {
        Page<ExamRoomResponse> items = examRoomService.search(keyword, fromDate, toDate, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("fromDate", fromDate == null ? "" : fromDate);
        model.addAttribute("toDate", toDate == null ? "" : toDate);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "exam-rooms/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("examRoomRequest", new ExamRoomRequest());
        model.addAttribute("rooms", examRoomService.getAllActiveRooms());
        return "exam-rooms/form";
    }

    @PostMapping
    public String create(
        @Valid @ModelAttribute("examRoomRequest") ExamRoomRequest req,
        BindingResult result,
        Model model,
        RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("rooms", examRoomService.getAllActiveRooms());
            return "exam-rooms/form";
        }
        try {
            examRoomService.create(req);
            redirect.addFlashAttribute("success", "Thêm phòng thi thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "create");
            model.addAttribute("rooms", examRoomService.getAllActiveRooms());
            model.addAttribute("globalError", e.getMessage());
            return "exam-rooms/form";
        }
        return "redirect:/admin/exam-rooms";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        ExamRoomResponse r = examRoomService.getById(id);
        ExamRoomRequest req = new ExamRoomRequest();
        req.setRoomId(r.roomId());
        req.setExamCapacity(r.examCapacity());
        req.setDescription(r.description());
        model.addAttribute("mode", "edit");
        model.addAttribute("examRoomId", id);
        model.addAttribute("examRoomRequest", req);
        model.addAttribute("rooms", examRoomService.getAllActiveRooms());
        model.addAttribute("currentItem", r);
        return "exam-rooms/form";
    }

    @PostMapping("/{id}")
    public String update(
        @PathVariable UUID id,
        @Valid @ModelAttribute("examRoomRequest") ExamRoomRequest req,
        BindingResult result,
        Model model,
        RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("examRoomId", id);
            model.addAttribute("rooms", examRoomService.getAllActiveRooms());
            return "exam-rooms/form";
        }
        try {
            examRoomService.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật phòng thi thành công");
        } catch (Exception e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("examRoomId", id);
            model.addAttribute("rooms", examRoomService.getAllActiveRooms());
            model.addAttribute("globalError", e.getMessage());
            return "exam-rooms/form";
        }
        return "redirect:/admin/exam-rooms";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            examRoomService.delete(id);
            redirect.addFlashAttribute("success", "Xóa phòng thi thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/exam-rooms";
    }

    @GetMapping("/print")
    public String print(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String fromDate,
        @RequestParam(required = false) String toDate,
        Model model
    ) {
        List<ExamRoomResponse> items = examRoomService.getAllFiltered(keyword, fromDate, toDate);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("fromDate", fromDate == null ? "" : fromDate);
        model.addAttribute("toDate", toDate == null ? "" : toDate);
        return "exam-rooms/print";
    }

    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        examRoomService.exportExcel(response);
    }

    @PostMapping("/import")
    public String importExcel(
        @RequestParam("file") MultipartFile file,
        RedirectAttributes redirect
    ) {
        try {
            int count = examRoomService.importExcel(file);
            redirect.addFlashAttribute("success", "Import thành công " + count + " phòng thi");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import thất bại: " + e.getMessage());
        }
        return "redirect:/admin/exam-rooms";
    }
}

package com.example.stduents_management.timeslot.controller;

import com.example.stduents_management.timeslot.dto.TimeSlotRequest;
import com.example.stduents_management.timeslot.dto.TimeSlotResponse;
import com.example.stduents_management.timeslot.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/time-slots")
@RequiredArgsConstructor
public class TimeSlotDashboardController {

    private final TimeSlotService service;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<TimeSlotResponse> timeSlots = service.search(keyword, page, size);
        model.addAttribute("timeSlots", timeSlots);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "time-slots/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("timeSlotRequest", new TimeSlotRequest());
        return "time-slots/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("timeSlotRequest") TimeSlotRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "time-slots/form";
        }
        try {
            service.create(req);
            redirect.addFlashAttribute("success", "Thêm khung giờ thành công");
            return "redirect:/admin/time-slots";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getReason());
            return "time-slots/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        TimeSlotResponse r = service.getById(id);
        TimeSlotRequest req = new TimeSlotRequest();
        req.setSlotCode(r.slotCode());
        req.setPeriodStart(r.periodStart());
        req.setPeriodEnd(r.periodEnd());
        req.setStartTime(r.startTime());
        req.setEndTime(r.endTime());
        req.setIsActive(r.isActive());
        model.addAttribute("mode", "edit");
        model.addAttribute("timeSlotId", id);
        model.addAttribute("timeSlotRequest", req);
        return "time-slots/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Integer id,
            @Valid @ModelAttribute("timeSlotRequest") TimeSlotRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("timeSlotId", id);
            return "time-slots/form";
        }
        try {
            service.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật khung giờ thành công");
            return "redirect:/admin/time-slots";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("timeSlotId", id);
            model.addAttribute("globalError", e.getReason());
            return "time-slots/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("success", "Xóa khung giờ thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/time-slots";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("timeSlots", service.getForPrint());
        return "time-slots/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            service.importExcel(file);
            redirect.addFlashAttribute("success", "Import khung giờ thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/time-slots";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = service.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=time_slots.xlsx");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}

package com.example.stduents_management.scheduleoverride.controller;

import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.schedule.repository.ScheduleRepository;
import com.example.stduents_management.scheduleoverride.dto.ScheduleOverrideRequest;
import com.example.stduents_management.scheduleoverride.dto.ScheduleOverrideResponse;
import com.example.stduents_management.scheduleoverride.entity.OverrideStatus;
import com.example.stduents_management.scheduleoverride.entity.OverrideType;
import com.example.stduents_management.scheduleoverride.service.ScheduleOverrideService;
import com.example.stduents_management.timeslot.repository.TimeSlotRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/schedule-overrides")
@RequiredArgsConstructor
public class ScheduleOverrideDashboardController {

    private final ScheduleOverrideService service;
    private final ScheduleRepository scheduleRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final LecturerRepository lecturerRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<ScheduleOverrideResponse> overrides = service.search(keyword, page, size);
        model.addAttribute("overrides", overrides);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "schedule-overrides/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("request", new ScheduleOverrideRequest());
        loadFormData(model);
        return "schedule-overrides/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("request") ScheduleOverrideRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            loadFormData(model);
            return "schedule-overrides/form";
        }
        try {
            service.create(req);
            redirect.addFlashAttribute("success", "Thêm thay đổi lịch thành công");
            return "redirect:/admin/schedule-overrides";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getReason());
            loadFormData(model);
            return "schedule-overrides/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        ScheduleOverrideResponse r = service.getById(id);
        ScheduleOverrideRequest req = new ScheduleOverrideRequest();
        req.setScheduleId(r.scheduleId());
        req.setOverrideDate(r.overrideDate());
        req.setOverrideType(r.overrideType());
        req.setNewRoomId(r.newRoomId());
        req.setNewTimeSlotId(r.newTimeSlotId());
        req.setNewLecturerId(r.newLecturerId());
        req.setStatus(r.status());
        req.setReason(r.reason());
        model.addAttribute("mode", "edit");
        model.addAttribute("overrideId", id);
        model.addAttribute("request", req);
        loadFormData(model);
        return "schedule-overrides/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("request") ScheduleOverrideRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("overrideId", id);
            loadFormData(model);
            return "schedule-overrides/form";
        }
        try {
            service.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật thay đổi lịch thành công");
            return "redirect:/admin/schedule-overrides";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("overrideId", id);
            model.addAttribute("globalError", e.getReason());
            loadFormData(model);
            return "schedule-overrides/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("success", "Xóa thay đổi lịch thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/schedule-overrides";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("overrides", service.getForPrint());
        return "schedule-overrides/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            service.importExcel(file);
            redirect.addFlashAttribute("success", "Import danh sách thay đổi lịch thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/schedule-overrides";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = service.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=schedule-overrides.xlsx");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private void loadFormData(Model model) {
        model.addAttribute("schedules", scheduleRepository.findAllForDropdown());
        model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
        model.addAttribute("timeSlots", timeSlotRepository.findAll(Sort.by("slotCode")));
        model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
        model.addAttribute("overrideTypes", OverrideType.values());
        model.addAttribute("overrideStatuses", OverrideStatus.values());
    }
}

package com.example.stduents_management.schedule.controller;

import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.schedule.dto.AutoScheduleRequest;
import com.example.stduents_management.schedule.dto.ScheduleRequest;
import com.example.stduents_management.schedule.dto.ScheduleResponse;
import com.example.stduents_management.schedule.entity.ScheduleStatus;
import com.example.stduents_management.schedule.entity.ScheduleType;
import com.example.stduents_management.schedule.entity.SessionType;
import com.example.stduents_management.schedule.entity.WeekPattern;
import com.example.stduents_management.schedule.service.ScheduleService;
import com.example.stduents_management.semester.repository.SemesterRepository;
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
@RequestMapping("/admin/schedules")
@RequiredArgsConstructor
public class ScheduleDashboardController {

    private final ScheduleService service;
    private final SemesterRepository semesterRepository;
    private final ClassSectionRepository classSectionRepository;
    private final LecturerRepository lecturerRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;

    @GetMapping("/calendar")
    public String calendar() {
        return "schedules/calendar";
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<ScheduleResponse> schedules = service.search(keyword, page, size);
        model.addAttribute("schedules", schedules);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "schedules/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("scheduleRequest", new ScheduleRequest());
        loadFormData(model);
        return "schedules/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("scheduleRequest") ScheduleRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            loadFormData(model);
            return "schedules/form";
        }
        try {
            service.create(req); // UUID ignored — redirect danh sách
            redirect.addFlashAttribute("success", "Thêm lịch học thành công");
            return "redirect:/admin/schedules";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "create");
            model.addAttribute("globalError", e.getReason());
            loadFormData(model);
            return "schedules/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        ScheduleResponse r = service.getById(id);
        ScheduleRequest req = new ScheduleRequest();
        req.setSemesterId(r.semesterId());
        req.setClassSectionId(r.classSectionId());
        req.setLecturerId(r.lecturerId());
        req.setRoomId(r.roomId());
        req.setTimeSlotId(r.timeSlotId());
        req.setDayOfWeek(r.dayOfWeek());
        req.setStartWeek(r.startWeek());
        req.setEndWeek(r.endWeek());
        req.setWeekPattern(r.weekPattern());
        req.setSessionType(r.sessionType());
        req.setScheduleType(r.scheduleType());
        req.setStatus(r.status());
        req.setNote(r.note());
        model.addAttribute("mode", "edit");
        model.addAttribute("scheduleId", id);
        model.addAttribute("scheduleRequest", req);
        loadFormData(model);
        return "schedules/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("scheduleRequest") ScheduleRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("scheduleId", id);
            loadFormData(model);
            return "schedules/form";
        }
        try {
            service.update(id, req);
            redirect.addFlashAttribute("success", "Cập nhật lịch học thành công");
            return "redirect:/admin/schedules";
        } catch (ResponseStatusException e) {
            model.addAttribute("mode", "edit");
            model.addAttribute("scheduleId", id);
            model.addAttribute("globalError", e.getReason());
            loadFormData(model);
            return "schedules/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("success", "Xóa lịch học thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason());
        }
        return "redirect:/admin/schedules";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("schedules", service.getForPrint());
        return "schedules/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            service.importExcel(file);
            redirect.addFlashAttribute("success", "Import lịch học thành công");
        } catch (ResponseStatusException e) {
            redirect.addFlashAttribute("error", e.getReason() != null ? e.getReason() : e.getMessage());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/schedules";
    }

    @GetMapping("/auto")
    public String autoForm(Model model) {
        model.addAttribute("autoScheduleRequest", new AutoScheduleRequest());
        model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        model.addAttribute("timeSlots", timeSlotRepository.findByIsActiveTrueOrderBySlotCode());
        return "schedules/auto";
    }

    @PostMapping("/auto")
    public String autoGenerate(
            @Valid @ModelAttribute("autoScheduleRequest") AutoScheduleRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
            model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
            model.addAttribute("timeSlots", timeSlotRepository.findByIsActiveTrueOrderBySlotCode());
            return "schedules/auto";
        }
        try {
            var res = service.generateAutoSchedule(req);
            if (res.getCreatedCount() <= 0) {
                model.addAttribute("autoScheduleRequest", req);
                model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
                model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
                model.addAttribute("timeSlots", timeSlotRepository.findByIsActiveTrueOrderBySlotCode());
                model.addAttribute("globalError", "Không thể phân lịch tự động cho dữ liệu đã chọn.");
                model.addAttribute("autoScheduleResultMessage", res.getMessage());
                model.addAttribute("autoScheduleSkippedDetails", res.getSkippedDetails());
                return "schedules/auto";
            }
            redirect.addFlashAttribute("success", res.getMessage());
            redirect.addFlashAttribute("autoScheduleCreated", res.getCreatedCount());
            redirect.addFlashAttribute("autoScheduleSkipped", res.getSkippedCount());
            return "redirect:/admin/schedules";
        } catch (ResponseStatusException e) {
            model.addAttribute("autoScheduleRequest", req);
            model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
            model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
            model.addAttribute("timeSlots", timeSlotRepository.findByIsActiveTrueOrderBySlotCode());
            model.addAttribute("globalError", e.getReason());
            return "schedules/auto";
        }
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = service.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=schedules.xlsx");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private void loadFormData(Model model) {
        model.addAttribute("semesters", semesterRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
        model.addAttribute("classSections", classSectionRepository.findAll(Sort.by("classCode")));
        model.addAttribute("lecturers", lecturerRepository.findAll(Sort.by("lecturerCode")));
        model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
        model.addAttribute("timeSlots", timeSlotRepository.findAll(Sort.by("slotCode")));
        model.addAttribute("weekPatterns", WeekPattern.values());
        model.addAttribute("sessionTypes", SessionType.values());
        model.addAttribute("scheduleTypes", ScheduleType.values());
        model.addAttribute("scheduleStatuses", ScheduleStatus.values());
    }
}

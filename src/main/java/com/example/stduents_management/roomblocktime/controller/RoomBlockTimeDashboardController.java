package com.example.stduents_management.roomblocktime.controller;

import com.example.stduents_management.room.repository.RoomRepository;
import com.example.stduents_management.roomblocktime.dto.RoomBlockTimeRequest;
import com.example.stduents_management.roomblocktime.dto.RoomBlockTimeResponse;
import com.example.stduents_management.roomblocktime.entity.BlockStatus;
import com.example.stduents_management.roomblocktime.entity.BlockType;
import com.example.stduents_management.roomblocktime.service.RoomBlockTimeService;
import com.example.stduents_management.timeslot.repository.TimeSlotRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
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
@RequestMapping("/admin/room-block-times")
@RequiredArgsConstructor
public class RoomBlockTimeDashboardController {

    private final RoomBlockTimeService roomBlockTimeService;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<RoomBlockTimeResponse> items = roomBlockTimeService.search(keyword, page, size);
        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "room-block-times/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        RoomBlockTimeRequest req = new RoomBlockTimeRequest();
        req.setStatus(BlockStatus.ACTIVE);
        model.addAttribute("mode", "create");
        model.addAttribute("roomBlockTimeRequest", req);
        loadSelectData(model);
        return "room-block-times/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("roomBlockTimeRequest") RoomBlockTimeRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            loadSelectData(model);
            return "room-block-times/form";
        }
        roomBlockTimeService.create(req);
        redirect.addFlashAttribute("success", "Thêm khóa phòng thành công");
        return "redirect:/admin/room-block-times";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        RoomBlockTimeResponse r = roomBlockTimeService.getById(id);
        RoomBlockTimeRequest req = new RoomBlockTimeRequest();
        req.setRoomId(r.roomId());
        req.setBlockType(r.blockType());
        req.setDayOfWeek(r.dayOfWeek());
        req.setTimeSlotId(r.timeSlotId());
        req.setStartWeek(r.startWeek());
        req.setEndWeek(r.endWeek());
        req.setStartDate(r.startDate());
        req.setEndDate(r.endDate());
        req.setReason(r.reason());
        req.setStatus(r.status());
        model.addAttribute("mode", "edit");
        model.addAttribute("blockId", id);
        model.addAttribute("roomBlockTimeRequest", req);
        loadSelectData(model);
        return "room-block-times/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("roomBlockTimeRequest") RoomBlockTimeRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("blockId", id);
            loadSelectData(model);
            return "room-block-times/form";
        }
        roomBlockTimeService.update(id, req);
        redirect.addFlashAttribute("success", "Cập nhật khóa phòng thành công");
        return "redirect:/admin/room-block-times";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        roomBlockTimeService.delete(id);
        redirect.addFlashAttribute("success", "Xóa khóa phòng thành công");
        return "redirect:/admin/room-block-times";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("items", roomBlockTimeService.getForPrint());
        return "room-block-times/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            roomBlockTimeService.importExcel(file);
            redirect.addFlashAttribute("success", "Import khóa phòng thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/room-block-times";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = roomBlockTimeService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=room-block-times.xlsx");
        headers.setContentLength(data.length);
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    private void loadSelectData(Model model) {
        model.addAttribute("rooms", roomRepository.findAll(org.springframework.data.domain.Sort.by("roomCode")));
        model.addAttribute("timeSlots", timeSlotRepository.findAll(org.springframework.data.domain.Sort.by("slotCode")));
        model.addAttribute("blockTypes", BlockType.values());
        model.addAttribute("statuses", BlockStatus.values());
    }
}

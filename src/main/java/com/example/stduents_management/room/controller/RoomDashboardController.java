package com.example.stduents_management.room.controller;

import com.example.stduents_management.building.repository.BuildingRepository;
import com.example.stduents_management.room.dto.RoomRequest;
import com.example.stduents_management.room.dto.RoomResponse;
import com.example.stduents_management.room.entity.RoomStatus;
import com.example.stduents_management.room.service.RoomService;
import com.example.stduents_management.roomtype.repository.RoomTypeRepository;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
public class RoomDashboardController {

    private final RoomService roomService;
    private final BuildingRepository buildingRepository;
    private final RoomTypeRepository roomTypeRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<RoomResponse> rooms = roomService.search(keyword, page, size);

        model.addAttribute("rooms", rooms);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "rooms/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        RoomRequest req = new RoomRequest();
        req.setStatus(RoomStatus.AVAILABLE);
        req.setIsActive(Boolean.TRUE);

        model.addAttribute("mode", "create");
        model.addAttribute("roomRequest", req);
        model.addAttribute("buildings", buildingRepository.findAll());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("statuses", RoomStatus.values());
        return "rooms/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("roomRequest") RoomRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("buildings", buildingRepository.findAll());
            model.addAttribute("roomTypes", roomTypeRepository.findAll());
            model.addAttribute("statuses", RoomStatus.values());
            return "rooms/form";
        }

        roomService.create(req);
        redirect.addFlashAttribute("success", "Thêm phòng học thành công");
        return "redirect:/admin/rooms";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model
    ) {
        RoomResponse r = roomService.getById(id);

        RoomRequest req = new RoomRequest();
        req.setRoomCode(r.roomCode());
        req.setRoomName(r.roomName());
        req.setBuildingId(r.buildingId());
        req.setRoomTypeId(r.roomTypeId());
        req.setFloor(r.floor());
        req.setCapacity(r.capacity());
        req.setArea(r.area());
        req.setStatus(r.status());
        req.setIsActive(r.isActive());

        model.addAttribute("mode", "edit");
        model.addAttribute("roomId", id);
        model.addAttribute("roomRequest", req);
        model.addAttribute("buildings", buildingRepository.findAll());
        model.addAttribute("roomTypes", roomTypeRepository.findAll());
        model.addAttribute("statuses", RoomStatus.values());

        return "rooms/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("roomRequest") RoomRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("roomId", id);
            model.addAttribute("buildings", buildingRepository.findAll());
            model.addAttribute("roomTypes", roomTypeRepository.findAll());
            model.addAttribute("statuses", RoomStatus.values());
            return "rooms/form";
        }

        roomService.update(id, req);
        redirect.addFlashAttribute("success", "Cập nhật phòng học thành công");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirect
    ) {
        roomService.delete(id);
        redirect.addFlashAttribute("success", "Xóa phòng học thành công");
        return "redirect:/admin/rooms";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("rooms", roomService.getForPrint());
        return "rooms/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            roomService.importExcel(file);
            redirect.addFlashAttribute("success", "Import phòng học thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {

        byte[] data = roomService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rooms.xlsx");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
package com.example.stduents_management.roomtype.controller;

import com.example.stduents_management.roomtype.dto.RoomTypeRequest;
import com.example.stduents_management.roomtype.dto.RoomTypeResponse;
import com.example.stduents_management.roomtype.service.RoomTypeService;
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

import java.util.UUID;

@Controller
@RequestMapping("/admin/room-types")
@RequiredArgsConstructor
public class RoomTypeDashboardController {

    private final RoomTypeService service;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {

        Page<RoomTypeResponse> roomTypes =
                service.search(keyword, page, size);

        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "room-types/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("roomTypeRequest", new RoomTypeRequest());
        return "room-types/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute RoomTypeRequest req,
            BindingResult result,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            return "room-types/form";
        }

        service.create(req);
        redirect.addFlashAttribute("success", "Thêm loại phòng thành công");
        return "redirect:/admin/room-types";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {

        RoomTypeResponse r = service.getById(id);

        RoomTypeRequest req = new RoomTypeRequest();
        req.setRoomTypeCode(r.roomTypeCode());
        req.setRoomTypeName(r.roomTypeName());
        req.setDescription(r.description());
        req.setMaxCapacity(r.maxCapacity());

        model.addAttribute("mode", "edit");
        model.addAttribute("roomTypeId", id);
        model.addAttribute("roomTypeRequest", req);

        return "room-types/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute RoomTypeRequest req,
            BindingResult result,
            RedirectAttributes redirect
    ) {

        if (result.hasErrors()) {
            return "room-types/form";
        }

        service.update(id, req);
        redirect.addFlashAttribute("success", "Cập nhật loại phòng thành công");
        return "redirect:/admin/room-types";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id,
                         RedirectAttributes redirect) {

        service.delete(id);
        redirect.addFlashAttribute("success", "Xóa loại phòng thành công");
        return "redirect:/admin/room-types";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("roomTypes", service.getForPrint());
        return "room-types/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            service.importExcel(file);
            redirect.addFlashAttribute("success", "Import loại phòng thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/room-types";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {

        byte[] data = service.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=room_types.xlsx");
        headers.setContentLength(data.length);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
package com.example.stduents_management.equipment.controller;

import com.example.stduents_management.equipment.dto.EquipmentRequest;
import com.example.stduents_management.equipment.dto.EquipmentResponse;
import com.example.stduents_management.equipment.entity.EquipmentStatus;
import com.example.stduents_management.equipment.service.EquipmentService;
import com.example.stduents_management.room.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;

@Controller
@RequestMapping("/admin/equipments")
@RequiredArgsConstructor
public class EquipmentDashboardController {

    private final EquipmentService equipmentService;
    private final RoomRepository roomRepository;

    @InitBinder("equipmentRequest")
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Long.class, "roomId", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.trim().isEmpty()) {
                    setValue(null);
                } else {
                    try {
                        setValue(Long.parseLong(text.trim()));
                    } catch (NumberFormatException e) {
                        setValue(null);
                    }
                }
            }
        });
    }

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<EquipmentResponse> equipments = equipmentService.search(keyword, page, size);
        model.addAttribute("equipments", equipments);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "equipments/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        EquipmentRequest req = new EquipmentRequest();
        req.setStatus(EquipmentStatus.ACTIVE);
        model.addAttribute("mode", "create");
        model.addAttribute("equipmentRequest", req);
        model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
        model.addAttribute("statuses", EquipmentStatus.values());
        return "equipments/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("equipmentRequest") EquipmentRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
            model.addAttribute("statuses", EquipmentStatus.values());
            return "equipments/form";
        }
        equipmentService.create(req);
        redirect.addFlashAttribute("success", "Thêm thiết bị thành công");
        return "redirect:/admin/equipments";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        EquipmentResponse r = equipmentService.getById(id);
        EquipmentRequest req = new EquipmentRequest();
        req.setEquipmentCode(r.equipmentCode());
        req.setEquipmentName(r.equipmentName());
        req.setSerialNumber(r.serialNumber());
        req.setPurchaseDate(r.purchaseDate());
        req.setStatus(r.status());
        req.setRoomId(r.roomId());
        model.addAttribute("mode", "edit");
        model.addAttribute("equipmentId", id);
        model.addAttribute("equipmentRequest", req);
        model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
        model.addAttribute("statuses", EquipmentStatus.values());
        return "equipments/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("equipmentRequest") EquipmentRequest req,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("equipmentId", id);
            model.addAttribute("rooms", roomRepository.findAll(Sort.by("roomCode")));
            model.addAttribute("statuses", EquipmentStatus.values());
            return "equipments/form";
        }
        equipmentService.update(id, req);
        redirect.addFlashAttribute("success", "Cập nhật thiết bị thành công");
        return "redirect:/admin/equipments";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        equipmentService.delete(id);
        redirect.addFlashAttribute("success", "Xóa thiết bị thành công");
        return "redirect:/admin/equipments";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("equipments", equipmentService.getForPrint());
        return "equipments/print";
    }

    @PostMapping("/import")
    public String importExcel(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirect
    ) {
        try {
            equipmentService.importExcel(file);
            redirect.addFlashAttribute("success", "Import thiết bị thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Import không thành công: " + e.getMessage());
        }
        return "redirect:/admin/equipments";
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportExcel() throws Exception {
        byte[] data = equipmentService.exportExcel();
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=equipments.xlsx");
        headers.setContentLength(data.length);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}

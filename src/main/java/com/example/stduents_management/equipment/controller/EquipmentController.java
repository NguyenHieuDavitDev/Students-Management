package com.example.stduents_management.equipment.controller;

import com.example.stduents_management.equipment.dto.EquipmentRequest;
import com.example.stduents_management.equipment.dto.EquipmentResponse;
import com.example.stduents_management.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<Page<EquipmentResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(equipmentService.search(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody EquipmentRequest req) {
        equipmentService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest req
    ) {
        equipmentService.update(id, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/print")
    public ResponseEntity<List<EquipmentResponse>> print() {
        return ResponseEntity.ok(equipmentService.getForPrint());
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        equipmentService.importExcel(file);
        return ResponseEntity.ok().build();
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

package com.example.stduents_management.building.controller;

import com.example.stduents_management.building.dto.BuildingRequest;
import com.example.stduents_management.building.dto.BuildingResponse;
import com.example.stduents_management.building.service.BuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService service;

    @GetMapping
    public Page<BuildingResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.search(keyword, page, size);
    }

    @PostMapping
    public void create(@RequestBody BuildingRequest req) {
        service.create(req);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable UUID id,
                       @RequestBody BuildingRequest req) {
        service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
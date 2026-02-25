package com.example.stduents_management.building.controller;


import com.example.stduents_management.building.dto.BuildingRequest;
import com.example.stduents_management.building.service.BuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin/buildings")
@RequiredArgsConstructor
public class BuildingDashboardController {

    private final BuildingService service;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        model.addAttribute("buildings",
                service.search(keyword, page, size));
        model.addAttribute("keyword", keyword);
        return "buildings/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("buildingRequest", new BuildingRequest());
        model.addAttribute("mode", "create");
        return "buildings/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute BuildingRequest req) {
        service.create(req);
        return "redirect:/admin/buildings";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable UUID id, Model model) {
        model.addAttribute("mode", "edit");
        model.addAttribute("buildingId", id);
        model.addAttribute("buildingRequest", service.getById(id));
        return "buildings/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable UUID id,
                         @Valid @ModelAttribute BuildingRequest req) {
        service.update(id, req);
        return "redirect:/admin/buildings";
    }

    @GetMapping("/print")
    public String print(Model model) {
        model.addAttribute("buildings", service.getForPrint());
        return "buildings/print";
    }
}
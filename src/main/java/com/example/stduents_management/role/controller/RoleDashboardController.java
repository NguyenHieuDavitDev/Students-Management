package com.example.stduents_management.role.controller;

import com.example.stduents_management.role.dto.RoleRequest;
import com.example.stduents_management.role.dto.RoleResponse;
import com.example.stduents_management.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleDashboardController {

    private final RoleService roleService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        Page<RoleResponse> roles = roleService.search(keyword, page, size);
        model.addAttribute("roles", roles);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "roles/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("roleId", null);
        model.addAttribute("roleRequest", new RoleRequest());
        return "roles/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("roleRequest") RoleRequest roleRequest,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "create");
        model.addAttribute("roleId", null);

        if (bindingResult.hasErrors()) {
            return "roles/form";
        }

        try {
            roleService.create(roleRequest);
            return "redirect:/admin/roles";
        } catch (ResponseStatusException ex) {
            model.addAttribute("globalError", ex.getReason());
            return "roles/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        RoleResponse role = roleService.getById(id);
        RoleRequest req = new RoleRequest();
        req.setName(role.getName());
        req.setDescription(role.getDescription());

        model.addAttribute("mode", "edit");
        model.addAttribute("roleId", id);
        model.addAttribute("roleRequest", req);
        return "roles/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("roleRequest") RoleRequest roleRequest,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "edit");
        model.addAttribute("roleId", id);

        if (bindingResult.hasErrors()) {
            return "roles/form";
        }

        try {
            roleService.update(id, roleRequest);
            return "redirect:/admin/roles";
        } catch (ResponseStatusException ex) {
            model.addAttribute("globalError", ex.getReason());
            return "roles/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        roleService.delete(id);
        return "redirect:/admin/roles";
    }
}


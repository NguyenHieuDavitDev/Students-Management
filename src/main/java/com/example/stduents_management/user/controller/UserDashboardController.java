package com.example.stduents_management.user.controller;

import com.example.stduents_management.role.service.RoleService;
import com.example.stduents_management.user.dto.UserRequest;
import com.example.stduents_management.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserDashboardController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        model.addAttribute("users", userService.search(keyword, page, size));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        return "users/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        model.addAttribute("userRequest", new UserRequest());
        model.addAttribute("roles", roleService.getAll());
        return "users/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("userRequest") UserRequest userRequest,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "create");
        model.addAttribute("userId", null);

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAll());
            return "users/form";
        }

        try {
            userService.create(userRequest);
            return "redirect:/admin/users";
        } catch (ResponseStatusException ex) {
            model.addAttribute("globalError", ex.getReason());
            model.addAttribute("roles", roleService.getAll());
            return "users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        var user = userService.getById(id);
        UserRequest req = new UserRequest();
        req.setUsername(user.getUsername());
        req.setEmail(user.getEmail());
        req.setEnabled(user.isEnabled());
        req.setRoleIds(user.getRoles().stream()
                .map(roleName -> roleService.getAll().stream()
                        .filter(r -> r.getName().equals(roleName))
                        .findFirst()
                        .map(r -> r.getId())
                        .orElse(null))
                .filter(id2 -> id2 != null)
                .collect(java.util.stream.Collectors.toSet()));

        model.addAttribute("mode", "edit");
        model.addAttribute("userId", id);
        model.addAttribute("userRequest", req);
        model.addAttribute("roles", roleService.getAll());
        return "users/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("userRequest") UserRequest userRequest,
            BindingResult bindingResult,
            Model model
    ) {
        model.addAttribute("mode", "edit");
        model.addAttribute("userId", id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAll());
            return "users/form";
        }

        try {
            userService.update(id, userRequest);
            return "redirect:/admin/users";
        } catch (ResponseStatusException ex) {
            model.addAttribute("globalError", ex.getReason());
            model.addAttribute("roles", roleService.getAll());
            return "users/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        userService.delete(id);
        return "redirect:/admin/users";
    }
}

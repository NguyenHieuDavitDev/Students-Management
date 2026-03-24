package com.example.stduents_management.systemsetting.controller;

import com.example.stduents_management.systemsetting.dto.SystemSettingsRequest;
import com.example.stduents_management.systemsetting.service.SystemSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingsDashboardController {

    private final SystemSettingsService systemSettingsService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("systemSettingsRequest", systemSettingsService.getForEdit());
        return "settings/index";
    }

    @PostMapping
    public String save(
            @Valid @ModelAttribute("systemSettingsRequest") SystemSettingsRequest systemSettingsRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "settings/index";
        }
        systemSettingsService.save(systemSettingsRequest);
        redirectAttributes.addFlashAttribute("success", "Đã lưu cấu hình hệ thống.");
        return "redirect:/admin/settings";
    }
}

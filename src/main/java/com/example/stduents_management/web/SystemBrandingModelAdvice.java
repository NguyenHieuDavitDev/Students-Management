package com.example.stduents_management.web;

import com.example.stduents_management.systemsetting.dto.SystemBrandingDto;
import com.example.stduents_management.systemsetting.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thêm {@code systemBranding} cho sidebar / footer (tên đơn vị, dòng chân trang).
 */
@ControllerAdvice
@RequiredArgsConstructor
public class SystemBrandingModelAdvice {

    private final SystemSettingsService systemSettingsService;

    @ModelAttribute("systemBranding")
    public SystemBrandingDto systemBranding() {
        return systemSettingsService.getBrandingForView();
    }
}

package com.example.stduents_management.systemsetting.dto;

/**
 * Dữ liệu hiển thị công khai (sidebar, footer) từ cấu hình hệ thống.
 */
public record SystemBrandingDto(String sidebarTitle, String footerText) {

    public static SystemBrandingDto empty() {
        return new SystemBrandingDto(null, null);
    }
}

package com.example.stduents_management.systemsetting.dto;

/**
 * Dữ liệu hiển thị từ cấu hình hệ thống (sidebar, footer, đăng nhập, thông báo chung).
 */
public record SystemBrandingDto(
        String sidebarTitle,
        String footerText,
        String logoUrl,
        String loginTitle,
        String loginTagline,
        String globalNotice,
        boolean globalNoticeEnabled
) {

    public static SystemBrandingDto empty() {
        return new SystemBrandingDto(null, null, null, null, null, null, false);
    }

    public boolean hasGlobalNotice() {
        return globalNoticeEnabled && globalNotice != null && !globalNotice.isBlank();
    }
}

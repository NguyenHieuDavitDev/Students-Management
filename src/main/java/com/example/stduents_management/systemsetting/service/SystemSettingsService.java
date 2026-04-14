package com.example.stduents_management.systemsetting.service;

import com.example.stduents_management.systemsetting.dto.SettingsAuditInfo;
import com.example.stduents_management.systemsetting.dto.SystemBrandingDto;
import com.example.stduents_management.systemsetting.dto.SystemSettingsRequest;
import com.example.stduents_management.systemsetting.entity.SystemSettings;
import com.example.stduents_management.systemsetting.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SystemSettingsService {

    private final SystemSettingsRepository repository;

    @Transactional(readOnly = true)
    public SystemBrandingDto getBrandingForView() {
        return repository.findById(SystemSettings.SINGLETON_ID)
                .map(this::toBranding)
                .orElse(SystemBrandingDto.empty());
    }

    /** Xem trước branding từ form (khi validation lỗi, chưa lưu DB). */
    public SystemBrandingDto previewFromRequest(SystemSettingsRequest r) {
        String sidebar = null;
        if (r.getSchoolShortName() != null && !r.getSchoolShortName().isBlank()) {
            sidebar = r.getSchoolShortName().trim();
        } else if (r.getSchoolName() != null && !r.getSchoolName().isBlank()) {
            sidebar = r.getSchoolName().trim();
        }
        String loginTitle = sidebar != null ? sidebar : "Students Management";
        String logo = trimToNull(r.getLogoUrl());
        String tagline = trimToNull(r.getLoginTagline());
        String notice = trimToNull(r.getGlobalNotice());
        return new SystemBrandingDto(
                sidebar,
                trimToNull(r.getFooterNote()),
                logo,
                loginTitle,
                tagline,
                notice,
                r.isGlobalNoticeEnabled()
        );
    }

    @Transactional
    public SystemSettingsRequest getForEdit() {
        return toRequest(getOrCreate());
    }

    @Transactional(readOnly = true)
    public SettingsAuditInfo getAuditInfo() {
        return repository.findById(SystemSettings.SINGLETON_ID)
                .map(s -> new SettingsAuditInfo(s.getUpdatedAt(), s.getLastUpdatedBy()))
                .orElse(SettingsAuditInfo.none());
    }

    @Transactional
    public void save(SystemSettingsRequest req, String updatedByUsername) {
        SystemSettings entity = getOrCreate();
        entity.setSchoolName(trimToNull(req.getSchoolName()));
        entity.setSchoolShortName(trimToNull(req.getSchoolShortName()));
        entity.setAddress(trimToNull(req.getAddress()));
        entity.setPhone(trimToNull(req.getPhone()));
        entity.setContactEmail(trimToNull(req.getContactEmail()));
        entity.setWebsite(trimToNull(req.getWebsite()));
        entity.setFooterNote(trimToNull(req.getFooterNote()));
        entity.setLogoUrl(trimToNull(req.getLogoUrl()));
        entity.setLoginTagline(trimToNull(req.getLoginTagline()));
        entity.setGlobalNotice(trimToNull(req.getGlobalNotice()));
        entity.setGlobalNoticeEnabled(req.isGlobalNoticeEnabled());
        entity.setUpdatedAt(Instant.now());
        if (updatedByUsername == null || updatedByUsername.isBlank()) {
            entity.setLastUpdatedBy(null);
        } else {
            String u = updatedByUsername.trim();
            entity.setLastUpdatedBy(u.length() > 100 ? u.substring(0, 100) : u);
        }
        repository.save(entity);
    }

    private SystemSettings getOrCreate() {
        return repository.findById(SystemSettings.SINGLETON_ID).orElseGet(() -> {
            SystemSettings s = new SystemSettings();
            s.setId(SystemSettings.SINGLETON_ID);
            s.setGlobalNoticeEnabled(Boolean.FALSE);
            return repository.save(s);
        });
    }

    private SystemSettingsRequest toRequest(SystemSettings s) {
        SystemSettingsRequest r = new SystemSettingsRequest();
        r.setSchoolName(s.getSchoolName());
        r.setSchoolShortName(s.getSchoolShortName());
        r.setAddress(s.getAddress());
        r.setPhone(s.getPhone());
        r.setContactEmail(s.getContactEmail());
        r.setWebsite(s.getWebsite());
        r.setFooterNote(s.getFooterNote());
        r.setLogoUrl(s.getLogoUrl());
        r.setLoginTagline(s.getLoginTagline());
        r.setGlobalNotice(s.getGlobalNotice());
        r.setGlobalNoticeEnabled(Boolean.TRUE.equals(s.getGlobalNoticeEnabled()));
        return r;
    }

    private SystemBrandingDto toBranding(SystemSettings s) {
        String sidebar = resolveSidebarTitle(s);
        String footer = trimOrNull(s.getFooterNote());
        String logo = trimOrNull(s.getLogoUrl());
        String loginTitle = sidebar != null ? sidebar : "Students Management";
        String tagline = trimOrNull(s.getLoginTagline());
        String notice = trimOrNull(s.getGlobalNotice());
        boolean noticeOn = Boolean.TRUE.equals(s.getGlobalNoticeEnabled());
        return new SystemBrandingDto(sidebar, footer, logo, loginTitle, tagline, notice, noticeOn);
    }

    private static String resolveSidebarTitle(SystemSettings s) {
        if (s.getSchoolShortName() != null && !s.getSchoolShortName().isBlank()) {
            return s.getSchoolShortName().trim();
        }
        if (s.getSchoolName() != null && !s.getSchoolName().isBlank()) {
            return s.getSchoolName().trim();
        }
        return null;
    }

    private static String trimOrNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String trimToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}

package com.example.stduents_management.systemsetting.service;

import com.example.stduents_management.systemsetting.dto.SystemBrandingDto;
import com.example.stduents_management.systemsetting.dto.SystemSettingsRequest;
import com.example.stduents_management.systemsetting.entity.SystemSettings;
import com.example.stduents_management.systemsetting.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public SystemSettingsRequest getForEdit() {
        return toRequest(getOrCreate());
    }

    @Transactional
    public void save(SystemSettingsRequest req) {
        SystemSettings entity = getOrCreate();
        entity.setSchoolName(trimToNull(req.getSchoolName()));
        entity.setSchoolShortName(trimToNull(req.getSchoolShortName()));
        entity.setAddress(trimToNull(req.getAddress()));
        entity.setPhone(trimToNull(req.getPhone()));
        entity.setContactEmail(trimToNull(req.getContactEmail()));
        entity.setWebsite(trimToNull(req.getWebsite()));
        entity.setFooterNote(trimToNull(req.getFooterNote()));
        repository.save(entity);
    }

    private SystemSettings getOrCreate() {
        return repository.findById(SystemSettings.SINGLETON_ID).orElseGet(() -> {
            SystemSettings s = new SystemSettings();
            s.setId(SystemSettings.SINGLETON_ID);
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
        return r;
    }

    private SystemBrandingDto toBranding(SystemSettings s) {
        String sidebar = null;
        if (s.getSchoolShortName() != null && !s.getSchoolShortName().isBlank()) {
            sidebar = s.getSchoolShortName().trim();
        } else if (s.getSchoolName() != null && !s.getSchoolName().isBlank()) {
            sidebar = s.getSchoolName().trim();
        }
        String footer = s.getFooterNote();
        if (footer != null && footer.isBlank()) {
            footer = null;
        } else if (footer != null) {
            footer = footer.trim();
        }
        return new SystemBrandingDto(sidebar, footer);
    }

    private static String trimToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}

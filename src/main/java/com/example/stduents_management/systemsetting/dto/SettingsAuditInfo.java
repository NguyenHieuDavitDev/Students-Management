package com.example.stduents_management.systemsetting.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record SettingsAuditInfo(Instant updatedAt, String lastUpdatedBy) {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    public static SettingsAuditInfo none() {
        return new SettingsAuditInfo(null, null);
    }

    public boolean hasAudit() {
        return updatedAt != null || (lastUpdatedBy != null && !lastUpdatedBy.isBlank());
    }

    public String updatedAtDisplay() {
        return updatedAt == null ? null : FMT.format(updatedAt);
    }
}

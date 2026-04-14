package com.example.stduents_management.systemsetting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettings {

    public static final int SINGLETON_ID = 1;

    @Id
    @Column(name = "id")
    private Integer id = SINGLETON_ID;

    @Column(name = "school_name", columnDefinition = "NVARCHAR(500)")
    private String schoolName;

    @Column(name = "school_short_name", columnDefinition = "NVARCHAR(200)")
    private String schoolShortName;

    @Column(name = "address", columnDefinition = "NVARCHAR(1000)")
    private String address;

    @Column(name = "phone", columnDefinition = "NVARCHAR(50)")
    private String phone;

    @Column(name = "contact_email", columnDefinition = "NVARCHAR(255)")
    private String contactEmail;

    @Column(name = "website", columnDefinition = "NVARCHAR(500)")
    private String website;

    @Column(name = "footer_note", columnDefinition = "NVARCHAR(500)")
    private String footerNote;

    @Column(name = "logo_url", columnDefinition = "NVARCHAR(500)")
    private String logoUrl;

    @Column(name = "login_tagline", columnDefinition = "NVARCHAR(500)")
    private String loginTagline;

    @Column(name = "global_notice", columnDefinition = "NVARCHAR(1000)")
    private String globalNotice;

    @Column(name = "global_notice_enabled", nullable = false)
    private Boolean globalNoticeEnabled = Boolean.FALSE;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "last_updated_by", columnDefinition = "NVARCHAR(100)")
    private String lastUpdatedBy;
}

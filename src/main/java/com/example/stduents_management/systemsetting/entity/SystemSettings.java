package com.example.stduents_management.systemsetting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}

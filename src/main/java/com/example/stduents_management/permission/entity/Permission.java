package com.example.stduents_management.permission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(nullable = false, unique = true, length = 80, columnDefinition = "NVARCHAR(80)")
    private String code;

    @Column(nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String description;

    /**
     * Khớp {@link com.example.stduents_management.permission.SidebarMenuDefinition#getMenuKey()} — khi gán quyền này cho vai trò,
     * mục sidebar tương ứng hiển thị (nếu bật chế độ menu theo quyền cho user đó).
     */
    @Column(name = "sidebar_menu_key", length = 64, columnDefinition = "NVARCHAR(64)")
    private String sidebarMenuKey;
}

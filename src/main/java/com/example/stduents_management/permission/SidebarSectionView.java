package com.example.stduents_management.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** Nhóm mục sidebar cho màn cấu hình theo vai trò. */
@Getter
@AllArgsConstructor
public class SidebarSectionView {
    private final String sectionId;
    private final String sectionTitleVi;
    private final List<SidebarMenuDefinition> items;
}

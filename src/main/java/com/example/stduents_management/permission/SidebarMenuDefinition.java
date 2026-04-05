package com.example.stduents_management.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Các mục menu sidebar admin; {@code menuKey} khớp với {@code activeMenu} trong {@code layout/sidebar.html}.
 * Gán {@link com.example.stduents_management.permission.entity.Permission#getSidebarMenuKey()} để khi vai trò có quyền đó,
 * mục tương ứng hiển thị (chế độ menu theo quyền).
 */
public enum SidebarMenuDefinition {
    AUDIT_LOGS("audit-logs", "USER_ADMIN", "Nhật ký hoạt động"),
    ROLES("roles", "USER_ADMIN", "Vai trò"),
    USERS("users", "USER_ADMIN", "Người dùng"),
    PERMISSIONS("permissions", "USER_ADMIN", "Quyền"),
    SETTINGS("settings", "USER_ADMIN", "Cấu hình hệ thống"),

    FACULTIES("faculties", "STUDENT", "Khoa"),
    MAJORS("majors", "STUDENT", "Ngành học"),
    CLASSES("classes", "STUDENT", "Lớp học"),
    EDUCATION_TYPES("education-types", "STUDENT", "Loại đào tạo"),
    TRAINING_LEVELS("trainingLevels", "STUDENT", "Bậc đào tạo"),
    STUDENTS("students", "STUDENT", "Sinh viên"),

    LECTURERS("lecturers", "LECTURER", "Giảng viên"),
    POSITIONS("positions", "LECTURER", "Hàm học vị"),
    DEPARTMENTS("departments", "LECTURER", "Phòng ban"),
    LECTURER_DUTIES("lecturer-duties", "LECTURER", "Chức vụ giảng viên"),

    TRAINING_PROGRAMS("training-programs", "PROGRAM", "Chương trình đào tạo"),
    COURSES("courses", "PROGRAM", "Môn học"),
    DOCUMENTS("documents", "PROGRAM", "Tài liệu học tập"),
    COURSE_PREREQUISITES("course-prerequisites", "PROGRAM", "Course Prerequisites"),
    GRADUATION_CONDITIONS("graduation-conditions", "PROGRAM", "Điều kiện xét tốt nghiệp"),
    GRADUATION_RESULTS("graduation-results", "PROGRAM", "Kết quả xét tốt nghiệp"),

    SEMESTERS("semesters", "SEMESTER", "Học kỳ"),
    CLASS_SECTIONS("class-sections", "SEMESTER", "Lớp học phần"),

    COURSE_REGISTRATIONS("course-registrations", "REGISTRATION", "Course Registrations"),

    BUILDINGS("buildings", "SCHEDULE", "Toà nhà"),
    ROOM_TYPES("room-types", "SCHEDULE", "Loại phòng"),
    ROOMS("rooms", "SCHEDULE", "Phòng học"),
    ROOM_BLOCK_TIMES("room-block-times", "SCHEDULE", "Khóa phòng"),
    TIME_SLOTS("time-slots", "SCHEDULE", "Khung giờ"),
    SCHEDULES("schedules", "SCHEDULE", "Lịch học"),
    SCHEDULE_OVERRIDES("schedule-overrides", "SCHEDULE", "Dạy bù / Đổi phòng"),
    LECTURER_COURSE_CLASSES("lecturer-course-classes", "SCHEDULE", "Phân công giảng viên"),
    EQUIPMENTS("equipments", "SCHEDULE", "Thiết bị"),

    TUITION_FEES("tuition-fees", "TUITION", "Cấu hình học phí"),
    STUDENT_TUITION("student-tuition", "TUITION", "Học phí sinh viên"),
    PAYMENTS("payments", "TUITION", "Lịch sử thanh toán"),

    EXAM_TYPES("exam-types", "EXAM", "Loại kỳ thi"),
    EXAM_ROOMS("exam-rooms", "EXAM", "Phân phòng thi"),
    EXAM_SCHEDULES("exam-schedules", "EXAM", "Lịch thi"),

    GRADE_COMPONENTS("grade-components", "GRADES", "Thành phần điểm"),
    STUDENT_GRADES("student-grades", "GRADES", "Điểm sinh viên"),
    ATTENDANCES("attendances", "GRADES", "Điểm danh"),
    FEEDBACKS("feedbacks", "GRADES", "Phản hồi GV & môn học"),
    /** Bảng điểm tổng kết + Thang điểm (cùng activeMenu grade-scales). */
    GRADE_SCALES("grade-scales", "GRADES", "Bảng điểm / Thang điểm");

    private final String menuKey;
    private final String sectionId;
    private final String labelVi;

    SidebarMenuDefinition(String menuKey, String sectionId, String labelVi) {
        this.menuKey = menuKey;
        this.sectionId = sectionId;
        this.labelVi = labelVi;
    }

    public String getMenuKey() {
        return menuKey;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getLabelVi() {
        return labelVi;
    }

    public static Optional<SidebarMenuDefinition> fromMenuKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        String t = key.trim();
        return Arrays.stream(values()).filter(d -> d.menuKey.equals(t)).findFirst();
    }

    public static boolean isValidMenuKeyOrBlank(String key) {
        if (key == null || key.isBlank()) {
            return true;
        }
        return fromMenuKey(key).isPresent();
    }

    public static Set<String> allSectionIds() {
        return Arrays.stream(values()).map(SidebarMenuDefinition::getSectionId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<String> sectionsForMenuKeys(Set<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return Set.of();
        }
        return keys.stream()
                .map(SidebarMenuDefinition::fromMenuKey)
                .flatMap(Optional::stream)
                .map(SidebarMenuDefinition::getSectionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Danh sách cho form (đã sắp theo nhãn). */
    public static List<SidebarMenuDefinition> sortedForForm() {
        return Arrays.stream(values())
                .sorted(Comparator.comparing(SidebarMenuDefinition::getLabelVi, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static final Map<String, String> SECTION_TITLES_VI = Map.ofEntries(
            Map.entry("USER_ADMIN", "Quản lý người dùng – Phân quyền (mục admin)"),
            Map.entry("STUDENT", "Quản lý sinh viên"),
            Map.entry("LECTURER", "Giảng viên – Nhân sự"),
            Map.entry("PROGRAM", "Chương trình – Môn học"),
            Map.entry("SEMESTER", "Học kỳ – Lớp học phần"),
            Map.entry("REGISTRATION", "Đăng ký học phần"),
            Map.entry("SCHEDULE", "Lịch học – Phòng học"),
            Map.entry("TUITION", "Học phí"),
            Map.entry("EXAM", "Loại kỳ thi – Khảo thí"),
            Map.entry("GRADES", "Điểm – Đánh giá học tập")
    );

    /** Thứ tự nhóm giống sidebar; dùng cho màn cấu hình menu theo vai trò. */
    public static List<SidebarSectionView> groupedSectionsInSidebarOrder() {
        Map<String, List<SidebarMenuDefinition>> map = new LinkedHashMap<>();
        for (SidebarMenuDefinition d : values()) {
            map.computeIfAbsent(d.sectionId, k -> new ArrayList<>()).add(d);
        }
        List<SidebarSectionView> out = new ArrayList<>();
        for (Map.Entry<String, List<SidebarMenuDefinition>> e : map.entrySet()) {
            String title = SECTION_TITLES_VI.getOrDefault(e.getKey(), e.getKey());
            out.add(new SidebarSectionView(e.getKey(), title, List.copyOf(e.getValue())));
        }
        return out;
    }
}

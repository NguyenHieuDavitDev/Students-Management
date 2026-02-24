package com.example.stduents_management.courseprerequisite.service;

import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.courseprerequisite.dto.CoursePrerequisiteResponse;
import com.example.stduents_management.courseprerequisite.entity.CoursePrerequisite;
import com.example.stduents_management.courseprerequisite.repository.CoursePrerequisiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoursePrerequisiteService {

    private final CourseRepository courseRepository;
    private final CoursePrerequisiteRepository coursePrerequisiteRepository;

    /* ================= SEARCH + PAGING ================= */
    @Transactional(readOnly = true)
    public Page<CoursePrerequisiteResponse> search(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("course.courseName").ascending()
                        .and(Sort.by("prerequisiteCourse.courseName").ascending()));

        Page<CoursePrerequisite> relations;

        if (keyword == null || keyword.isBlank()) {
            relations = coursePrerequisiteRepository.findAll(pageable);
        } else {
            relations = coursePrerequisiteRepository
                    .findByCourse_CourseCodeContainingIgnoreCaseOrCourse_CourseNameContainingIgnoreCaseOrPrerequisiteCourse_CourseCodeContainingIgnoreCaseOrPrerequisiteCourse_CourseNameContainingIgnoreCase(
                            keyword, keyword, keyword, keyword, pageable
                    );
        }

        return relations.map(this::mapToResponse);
    }

    /* ================= GET BY ID ================= */
    @Transactional(readOnly = true)
    public CoursePrerequisiteResponse getById(UUID id) {
        CoursePrerequisite rel = coursePrerequisiteRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần tiên quyết"));
        return mapToResponse(rel);
    }

    /* ================= CREATE SINGLE RELATION ================= */
    @Transactional
    public CoursePrerequisiteResponse create(UUID courseId, UUID prerequisiteCourseId) {

        if (courseId == null || prerequisiteCourseId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn đầy đủ học phần và học phần tiên quyết");
        }

        if (courseId.equals(prerequisiteCourseId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Học phần không thể là tiên quyết của chính nó");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần"));

        Course preCourse = courseRepository.findById(prerequisiteCourseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần tiên quyết"));

        if (coursePrerequisiteRepository.existsByCourseAndPrerequisiteCourse(course, preCourse)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Quan hệ học phần tiên quyết đã tồn tại");
        }

        CoursePrerequisite rel = new CoursePrerequisite();
        rel.setCourse(course);
        rel.setPrerequisiteCourse(preCourse);

        return mapToResponse(coursePrerequisiteRepository.save(rel));
    }

    /* ================= UPDATE SINGLE RELATION ================= */
    @Transactional
    public CoursePrerequisiteResponse update(UUID id, UUID courseId, UUID prerequisiteCourseId) {

        CoursePrerequisite existing = coursePrerequisiteRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần tiên quyết"));

        if (courseId == null || prerequisiteCourseId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Vui lòng chọn đầy đủ học phần và học phần tiên quyết");
        }

        if (courseId.equals(prerequisiteCourseId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Học phần không thể là tiên quyết của chính nó");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần"));

        Course preCourse = courseRepository.findById(prerequisiteCourseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần tiên quyết"));

        // Nếu thay đổi cặp course - prerequisite, kiểm tra trùng
        if ((existing.getCourse() == null || !existing.getCourse().getId().equals(courseId))
                || (existing.getPrerequisiteCourse() == null || !existing.getPrerequisiteCourse().getId().equals(prerequisiteCourseId))) {
            if (coursePrerequisiteRepository.existsByCourseAndPrerequisiteCourse(course, preCourse)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Quan hệ học phần tiên quyết đã tồn tại");
            }
        }

        existing.setCourse(course);
        existing.setPrerequisiteCourse(preCourse);

        return mapToResponse(coursePrerequisiteRepository.save(existing));
    }

    /* ================= GET IDS BY COURSE ================= */
    @Transactional(readOnly = true)
    public List<UUID> getPrerequisiteIdsByCourseId(UUID courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần"));

        return coursePrerequisiteRepository.findByCourse(course)
                .stream()
                .map(rel -> rel.getPrerequisiteCourse().getId())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CoursePrerequisiteResponse> getPrerequisitesByCourseId(UUID courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần"));

        List<CoursePrerequisite> relations = coursePrerequisiteRepository.findByCourse(course);

        return relations.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void updatePrerequisites(UUID courseId, List<UUID> prerequisiteIds) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần"));

        // Xóa toàn bộ quan hệ cũ
        coursePrerequisiteRepository.deleteByCourse(course);

        if (prerequisiteIds == null || prerequisiteIds.isEmpty()) {
            return;
        }

        for (UUID preId : prerequisiteIds) {

            if (preId == null || preId.equals(courseId)) {
                // Bỏ qua nếu null hoặc tự tham chiếu chính nó
                continue;
            }

            Course preCourse = courseRepository.findById(preId)
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy học phần tiên quyết"));

            // Tránh trùng lặp
            if (coursePrerequisiteRepository.existsByCourseAndPrerequisiteCourse(course, preCourse)) {
                continue;
            }

            CoursePrerequisite rel = new CoursePrerequisite();
            rel.setCourse(course);
            rel.setPrerequisiteCourse(preCourse);

            coursePrerequisiteRepository.save(rel);
        }
    }

    @Transactional
    public void updatePrerequisitesByCourseAndPreCourseCode(String courseCode, String preCourseCode) {

        if (courseCode == null || courseCode.isBlank()
                || preCourseCode == null || preCourseCode.isBlank()) {
            return;
        }

        Course course = courseRepository.findByCourseCodeIgnoreCase(courseCode.trim())
                .orElse(null);
        Course preCourse = courseRepository.findByCourseCodeIgnoreCase(preCourseCode.trim())
                .orElse(null);

        if (course == null || preCourse == null || course.getId().equals(preCourse.getId())) {
            return;
        }

        if (coursePrerequisiteRepository.existsByCourseAndPrerequisiteCourse(course, preCourse)) {
            return;
        }

        CoursePrerequisite rel = new CoursePrerequisite();
        rel.setCourse(course);
        rel.setPrerequisiteCourse(preCourse);
        coursePrerequisiteRepository.save(rel);
    }

    /* ================= DELETE ================= */
    @Transactional
    public void delete(UUID id) {
        if (!coursePrerequisiteRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy quan hệ học phần tiên quyết");
        }
        coursePrerequisiteRepository.deleteById(id);
    }

    /* ================= EXPORT EXCEL ================= */
    @Transactional(readOnly = true)
    public List<CoursePrerequisiteResponse> getForPrint() {
        return coursePrerequisiteRepository.findAll(Sort.by("course.courseName")
                        .and(Sort.by("prerequisiteCourse.courseName")))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CoursePrerequisiteResponse mapToResponse(CoursePrerequisite rel) {
        Course course = rel.getCourse();
        Course pre = rel.getPrerequisiteCourse();

        return CoursePrerequisiteResponse.builder()
                .id(rel.getId())
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseName(course.getCourseName())
                .prerequisiteCourseId(pre.getId())
                .prerequisiteCourseCode(pre.getCourseCode())
                .prerequisiteCourseName(pre.getCourseName())
                .build();
    }
}


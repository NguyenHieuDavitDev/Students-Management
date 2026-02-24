package com.example.stduents_management.courseprerequisite.controller;

import com.example.stduents_management.courseprerequisite.dto.CoursePrerequisiteResponse;
import com.example.stduents_management.courseprerequisite.service.CoursePrerequisiteService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/course-prerequisites")
@RequiredArgsConstructor
public class CoursePrerequisiteController {

    private final CoursePrerequisiteService coursePrerequisiteService;

    /* ================= CREATE ================= */
    @PostMapping
    public ResponseEntity<CoursePrerequisiteResponse> create(
            @RequestParam UUID courseId,
            @RequestParam UUID prerequisiteCourseId
    ) {
        CoursePrerequisiteResponse resp = coursePrerequisiteService.create(courseId, prerequisiteCourseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /* ================= GET BY ID ================= */
    @GetMapping("/{id}")
    public ResponseEntity<CoursePrerequisiteResponse> getById(@PathVariable UUID id) {
        CoursePrerequisiteResponse resp = coursePrerequisiteService.getById(id);
        return ResponseEntity.ok(resp);
    }

    /* ================= SEARCH + PAGING ================= */
    @GetMapping
    public ResponseEntity<Page<CoursePrerequisiteResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CoursePrerequisiteResponse> result = coursePrerequisiteService.search(keyword, page, size);
        return ResponseEntity.ok(result);
    }

    /* ================= BY COURSE ================= */
    @GetMapping("/by-course/{courseId}")
    public ResponseEntity<List<CoursePrerequisiteResponse>> getByCourse(@PathVariable UUID courseId) {
        List<CoursePrerequisiteResponse> list = coursePrerequisiteService.getPrerequisitesByCourseId(courseId);
        return ResponseEntity.ok(list);
    }

    /* ================= UPDATE SINGLE RELATION ================= */
    @PutMapping("/{id}")
    public ResponseEntity<CoursePrerequisiteResponse> update(
            @PathVariable UUID id,
            @RequestParam UUID courseId,
            @RequestParam UUID prerequisiteCourseId
    ) {
        CoursePrerequisiteResponse resp = coursePrerequisiteService.update(id, courseId, prerequisiteCourseId);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/by-course/{courseId}")
    public ResponseEntity<Void> updateByCourse(
            @PathVariable UUID courseId,
            @RequestBody List<UUID> prerequisiteIds
    ) {
        coursePrerequisiteService.updatePrerequisites(courseId, prerequisiteIds);
        return ResponseEntity.noContent().build();
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        coursePrerequisiteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /* ================= EXPORT ================= */
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("CoursePrerequisites");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã học phần");
            header.createCell(1).setCellValue("Tên học phần");
            header.createCell(2).setCellValue("Mã học phần tiên quyết");
            header.createCell(3).setCellValue("Tên học phần tiên quyết");

            List<CoursePrerequisiteResponse> relations = coursePrerequisiteService.getForPrint();

            int rowIndex = 1;

            for (CoursePrerequisiteResponse rel : relations) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rel.getCourseCode());
                row.createCell(1).setCellValue(rel.getCourseName());
                row.createCell(2).setCellValue(rel.getPrerequisiteCourseCode());
                row.createCell(3).setCellValue(rel.getPrerequisiteCourseName());
            }

            response.reset();
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=course-prerequisites.xlsx");

            workbook.write(response.getOutputStream());
            response.flushBuffer();

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể export Excel học phần tiên quyết");
        }
    }

    /* ================= IMPORT ================= */
    @PostMapping("/import")
    public ResponseEntity<String> importExcel(
            @RequestParam("file") MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Vui lòng upload file Excel (.xlsx)");
        }

        int count;
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            count = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String courseCode = row.getCell(0).getStringCellValue().trim();
                String preCourseCode = row.getCell(2).getStringCellValue().trim();

                coursePrerequisiteService.updatePrerequisitesByCourseAndPreCourseCode(courseCode, preCourseCode);
                count++;
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("File Excel không hợp lệ");
        }

        return ResponseEntity.ok("Đã import " + count + " quan hệ học phần tiên quyết");
    }

    /* ================= PRINT ================= */
    @GetMapping("/print")
    public ResponseEntity<List<CoursePrerequisiteResponse>> print() {
        List<CoursePrerequisiteResponse> list = coursePrerequisiteService.getForPrint();
        return ResponseEntity.ok(list);
    }
}



package com.example.stduents_management.examschedule.service;

import com.example.stduents_management.classsection.entity.ClassSection;
import com.example.stduents_management.classsection.repository.ClassSectionRepository;
import com.example.stduents_management.courseregistration.entity.CourseRegistration;
import com.example.stduents_management.courseregistration.repository.CourseRegistrationRepository;
import com.example.stduents_management.notification.entity.NotificationCategory;
import com.example.stduents_management.notification.service.NotificationService;
import com.example.stduents_management.examschedule.dto.ExamScheduleRequest;
import com.example.stduents_management.examschedule.dto.ExamScheduleResponse;
import com.example.stduents_management.examschedule.entity.ExamSchedule;
import com.example.stduents_management.examschedule.repository.ExamScheduleRepository;
import com.example.stduents_management.examtype.entity.ExamType;
import com.example.stduents_management.examtype.repository.ExamTypeRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final ClassSectionRepository classSectionRepository;
    private final ExamTypeRepository examTypeRepository;
    private final CourseRegistrationRepository courseRegistrationRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public Page<ExamScheduleResponse> search(
            String keyword,
            String fromDateStr,
            String toDateStr,
            int page,
            int size
    ) {
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        LocalDate from = parseDate(fromDateStr);
        LocalDate to = parseDate(toDateStr);
        Pageable pageable = PageRequest.of(page, size, Sort.by("examDate").ascending().and(Sort.by("startTime")));
        return examScheduleRepository.search(term, from, to, pageable).map(this::toResponse);
    }

    public List<ExamScheduleResponse> getAllFiltered(String keyword, String fromDateStr, String toDateStr) {
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        LocalDate from = parseDate(fromDateStr);
        LocalDate to = parseDate(toDateStr);
        return examScheduleRepository.search(term, from, to, Pageable.unpaged())
                .stream().map(this::toResponse).toList();
    }

    public ExamScheduleResponse getById(UUID id) {
        return examScheduleRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lịch thi"));
    }

    @Transactional
    public void create(ExamScheduleRequest req) {
        ClassSection cs = resolveClassSection(req.getClassSectionId());
        ExamType et = resolveExamType(req.getExamTypeId());

        ExamSchedule es = new ExamSchedule();
        buildEntity(es, req, cs, et);
        ExamSchedule saved = examScheduleRepository.save(es);
        notifyExamScheduleChange(saved.getId(), cs, et.getName(), req.getExamDate(), req.getStartTime(), req.getDurationMinutes(), req.getNote());
    }

    @Transactional
    public void update(UUID id, ExamScheduleRequest req) {
        ExamSchedule es = examScheduleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lịch thi"));
        ClassSection cs = resolveClassSection(req.getClassSectionId());
        ExamType et = resolveExamType(req.getExamTypeId());
        buildEntity(es, req, cs, et);
        examScheduleRepository.save(es);
        notifyExamScheduleChange(id, cs, et.getName(), req.getExamDate(), req.getStartTime(), req.getDurationMinutes(), req.getNote());
    }

    @Transactional
    public void delete(UUID id) {
        if (!examScheduleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lịch thi");
        }
        examScheduleRepository.deleteById(id);
        notificationService.deleteBySource(NotificationCategory.EXAM_SCHEDULE.name(), id.toString());
    }

    public void exportExcel(HttpServletResponse response) {
        List<ExamSchedule> all = examScheduleRepository.findAllOrdered();
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Lich thi");
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 10000);
            sheet.setColumnWidth(3, 8000);
            sheet.setColumnWidth(4, 5000);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 6000);

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LỊCH THI HỌC PHẦN");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Row header = sheet.createRow(1);
            String[] headers = {
                    "Mã LHP", "Mã học phần", "Tên học phần", "Học kỳ",
                    "Loại kỳ thi", "Ngày thi", "Giờ thi / Phút"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = wb.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowIdx = 2;
            for (ExamSchedule es : all) {
                Row row = sheet.createRow(rowIdx++);
                ClassSection cs = es.getClassSection();
                var c = cs.getCourse();
                row.createCell(0).setCellValue(cs.getClassCode());
                row.createCell(1).setCellValue(c.getCourseCode());
                row.createCell(2).setCellValue(c.getCourseName());
                row.createCell(3).setCellValue(cs.getSemester() != null ? cs.getSemester().getName() : "");
                row.createCell(4).setCellValue(es.getExamType() != null ? es.getExamType().getName() : "");
                row.createCell(5).setCellValue(es.getExamDate().format(DATE_FMT));
                row.createCell(6).setCellValue(
                        es.getStartTime().format(TIME_FMT) + " / " + es.getDurationMinutes() + " phút");
                for (int cIdx = 0; cIdx < 7; cIdx++) {
                    row.getCell(cIdx).setCellStyle(dataStyle);
                }
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=exam_schedules.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể xuất file Excel: " + e.getMessage());
        }
    }

    // helpers

    private ClassSection resolveClassSection(Long id) {
        return classSectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lớp học phần"));
    }

    private ExamType resolveExamType(UUID id) {
        return examTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy loại kỳ thi"));
    }

    private void buildEntity(ExamSchedule es, ExamScheduleRequest req,
                             ClassSection cs, ExamType et) {
        es.setClassSection(cs);
        es.setExamType(et);
        es.setExamDate(req.getExamDate());
        es.setStartTime(req.getStartTime());
        es.setDurationMinutes(req.getDurationMinutes());
        es.setNote(req.getNote() != null && !req.getNote().isBlank()
                ? req.getNote().trim() : null);
    }

    private void notifyExamScheduleChange(
            UUID examScheduleId,
            ClassSection cs,
            String examTypeName,
            LocalDate examDate,
            java.time.LocalTime startTime,
            Integer durationMinutes,
            String note
    ) {
        if (cs == null || cs.getId() == null) return;
        String title = NotificationCategory.EXAM_SCHEDULE.getLabel();
        String courseName = cs.getCourse() != null ? cs.getCourse().getCourseName() : "";
        String semesterName = cs.getSemester() != null ? cs.getSemester().getName() : "";
        String classCode = cs.getClassCode();

        String content = "Lịch thi đã được cập nhật: lớp " + (classCode != null ? classCode : "")
                + (courseName.isBlank() ? "" : " - " + courseName)
                + (semesterName.isBlank() ? "" : " (" + semesterName + ")")
                + ". Ngày thi: " + (examDate != null ? examDate : "")
                + ", Giờ: " + (startTime != null ? startTime : "")
                + ", Thời lượng: " + (durationMinutes != null ? durationMinutes : 0) + " phút"
                + ". Loại kỳ thi: " + (examTypeName != null ? examTypeName : "")
                + (note != null && !note.isBlank() ? ". Ghi chú: " + note.trim() : "")
                + ".";

        java.time.LocalDateTime scheduledAt = (examDate != null && startTime != null)
                ? java.time.LocalDateTime.of(examDate, startTime)
                : null;

        java.util.List<CourseRegistration> regs = courseRegistrationRepository
                .findByClassSection_IdOrderByStudent_FullName(cs.getId());

        for (CourseRegistration cr : regs) {
            if (cr == null || cr.getStudent() == null || cr.getStudent().getUser() == null) continue;
            notificationService.upsertForUserBySource(
                    cr.getStudent().getUser().getId(),
                    NotificationCategory.EXAM_SCHEDULE,
                    title,
                    content,
                    scheduledAt,
                    NotificationCategory.EXAM_SCHEDULE.name(),
                    examScheduleId != null ? examScheduleId.toString() : null
            );
        }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    private ExamScheduleResponse toResponse(ExamSchedule es) {
        ClassSection cs = es.getClassSection();
        var c = cs.getCourse();
        var sem = cs.getSemester();
        return new ExamScheduleResponse(
                es.getId(),
                cs.getId(),
                cs.getClassCode(),
                c.getCourseCode(),
                c.getCourseName(),
                sem != null ? sem.getName() : "",
                es.getExamType().getId(),
                es.getExamType().getName(),
                es.getExamDate(),
                es.getStartTime(),
                es.getDurationMinutes(),
                es.getNote(),
                es.getCreatedAt(),
                es.getUpdatedAt()
        );
    }
}
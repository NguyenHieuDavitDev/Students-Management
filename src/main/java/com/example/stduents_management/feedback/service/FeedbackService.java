package com.example.stduents_management.feedback.service;

import com.example.stduents_management.course.entity.Course;
import com.example.stduents_management.course.repository.CourseRepository;
import com.example.stduents_management.feedback.dto.FeedbackRequest;
import com.example.stduents_management.feedback.dto.FeedbackResponse;
import com.example.stduents_management.feedback.entity.TeachingFeedback;
import com.example.stduents_management.feedback.repository.TeachingFeedbackRepository;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.student.repository.StudentRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final TeachingFeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final CourseRepository courseRepository;

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Page<FeedbackResponse> search(
            String keyword,
            String fromDateStr,
            String toDateStr,
            int page,
            int size
    ) {
        String kw = normalizeKeyword(keyword);
        Integer ratingMatch = parseRatingDigitKeyword(kw);
        LocalDateTime from = parseDateStart(fromDateStr);
        LocalDateTime to = parseDateEnd(toDateStr);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return feedbackRepository.search(kw, ratingMatch, from, to, pageable).map(this::toResponse);
    }

    public List<FeedbackResponse> getAllFiltered(String keyword, String fromDateStr, String toDateStr) {
        String kw = normalizeKeyword(keyword);
        Integer ratingMatch = parseRatingDigitKeyword(kw);
        LocalDateTime from = parseDateStart(fromDateStr);
        LocalDateTime to = parseDateEnd(toDateStr);
        return feedbackRepository.search(kw, ratingMatch, from, to, Pageable.unpaged())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FeedbackResponse getById(UUID id) {
        TeachingFeedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phản hồi"));
        return toResponse(f);
    }

    @Transactional
    public FeedbackResponse create(FeedbackRequest req) {
        TeachingFeedback f = new TeachingFeedback();
        applyRequest(f, req);
        feedbackRepository.save(f);
        return toResponse(f);
    }

    @Transactional
    public FeedbackResponse update(UUID id, FeedbackRequest req) {
        TeachingFeedback f = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phản hồi"));
        applyRequest(f, req);
        feedbackRepository.save(f);
        return toResponse(f);
    }

    @Transactional
    public void delete(UUID id) {
        if (!feedbackRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phản hồi");
        }
        feedbackRepository.deleteById(id);
    }

    public void exportExcel(HttpServletResponse response) {
        List<TeachingFeedback> all = feedbackRepository.findAllForExport();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Phản hồi đánh giá");
            int[] widths = {4000, 3600, 14000, 3600, 14000, 5000, 10000, 2000, 12000, 6000};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i]);
            }

            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("PHẢN HỒI ĐÁNH GIÁ GIẢNG VIÊN / MÔN HỌC");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            CellStyle headerStyle = headerStyle(wb);

            Row header = sheet.createRow(1);
            String[] headers = {
                    "ID phản hồi", "Mã SV", "Họ tên SV", "Mã GV", "Họ tên GV",
                    "Mã môn", "Tên môn", "Điểm (1-5)", "Nhận xét", "Thời gian gửi"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = dataStyle(wb);
            int rowIdx = 2;
            for (TeachingFeedback f : all) {
                Row row = sheet.createRow(rowIdx++);
                FeedbackResponse r = toResponse(f);
                row.createCell(0).setCellValue(r.feedbackId() != null ? r.feedbackId().toString() : "");
                row.createCell(1).setCellValue(r.studentCode() != null ? r.studentCode() : "");
                row.createCell(2).setCellValue(r.studentName() != null ? r.studentName() : "");
                row.createCell(3).setCellValue(r.lecturerCode() != null ? r.lecturerCode() : "");
                row.createCell(4).setCellValue(r.lecturerName() != null ? r.lecturerName() : "");
                row.createCell(5).setCellValue(r.courseCode() != null ? r.courseCode() : "");
                row.createCell(6).setCellValue(r.courseName() != null ? r.courseName() : "");
                row.createCell(7).setCellValue(r.rating() != null ? r.rating() : 0);
                row.createCell(8).setCellValue(r.comment() != null ? r.comment() : "");
                row.createCell(9).setCellValue(
                        r.createdAt() != null ? r.createdAt().format(DATETIME_FMT) : ""
                );
                for (int c = 0; c < 10; c++) {
                    row.getCell(c).setCellStyle(dataStyle);
                }
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=feedbacks.xlsx");
            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể xuất file Excel: " + ex.getMessage());
        }
    }

    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File rỗng");
        }
        int count = 0;
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                try {
                    String c0 = getCellString(row.getCell(0));
                    String studentCode;
                    String lecturerCode;
                    String courseCode;
                    String ratingStr;
                    String comment;
                    if (c0.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
                        studentCode = getCellString(row.getCell(1));
                        lecturerCode = getCellString(row.getCell(2));
                        courseCode = getCellString(row.getCell(3));
                        ratingStr = getCellString(row.getCell(4));
                        comment = getCellString(row.getCell(5));
                    } else {
                        studentCode = c0;
                        lecturerCode = getCellString(row.getCell(1));
                        courseCode = getCellString(row.getCell(2));
                        ratingStr = getCellString(row.getCell(3));
                        comment = getCellString(row.getCell(4));
                    }

                    if (studentCode.isBlank() || lecturerCode.isBlank() || courseCode.isBlank() || ratingStr.isBlank()) {
                        continue;
                    }
                    int rating = (int) Double.parseDouble(ratingStr.replace(",", ".").trim());
                    if (rating < 1 || rating > 5) {
                        continue;
                    }

                    Student student = studentRepository.findByStudentCodeIgnoreCase(studentCode.trim())
                            .orElse(null);
                    Lecturer lecturer = lecturerRepository.findByLecturerCodeIgnoreCase(lecturerCode.trim())
                            .orElse(null);
                    Course course = courseRepository.findByCourseCodeIgnoreCase(courseCode.trim())
                            .orElse(null);
                    if (student == null || lecturer == null || course == null) {
                        continue;
                    }

                    TeachingFeedback f = new TeachingFeedback();
                    f.setStudent(student);
                    f.setLecturer(lecturer);
                    f.setSubject(course);
                    f.setRating(rating);
                    f.setComment(comment.isBlank() ? null : comment.trim());
                    feedbackRepository.save(f);
                    count++;
                } catch (Exception ignore) {
                    // bỏ qua dòng lỗi
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ: " + e.getMessage());
        }
        return count;
    }

    private void applyRequest(TeachingFeedback f, FeedbackRequest req) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy sinh viên"));
        Lecturer lecturer = lecturerRepository.findById(req.getLecturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy giảng viên"));
        Course subject = courseRepository.findById(req.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không tìm thấy môn học"));

        f.setStudent(student);
        f.setLecturer(lecturer);
        f.setSubject(subject);
        f.setRating(req.getRating());
        f.setComment(req.getComment() != null && !req.getComment().isBlank() ? req.getComment().trim() : null);
    }

    private FeedbackResponse toResponse(TeachingFeedback f) {
        Student s = f.getStudent();
        Lecturer l = f.getLecturer();
        Course c = f.getSubject();
        return new FeedbackResponse(
                f.getFeedbackId(),
                s.getStudentId(),
                s.getStudentCode(),
                s.getFullName(),
                l.getLecturerId(),
                l.getLecturerCode(),
                l.getFullName(),
                c.getId(),
                c.getCourseCode(),
                c.getCourseName(),
                f.getRating(),
                f.getComment(),
                f.getCreatedAt()
        );
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    /** Khi từ khóa là một chữ số 1–5, thêm khớp theo cột rating. */
    private static Integer parseRatingDigitKeyword(String kw) {
        if (kw == null || kw.length() != 1 || !Character.isDigit(kw.charAt(0))) {
            return null;
        }
        int d = kw.charAt(0) - '0';
        return (d >= 1 && d <= 5) ? d : null;
    }

    private static LocalDateTime parseDateStart(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s).atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDateTime parseDateEnd(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s).atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }

    private static CellStyle headerStyle(Workbook wb) {
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
        return headerStyle;
    }

    private static CellStyle dataStyle(Workbook wb) {
        CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        return dataStyle;
    }

    private static String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}

package com.example.stduents_management.trainingprogram.service;

import com.example.stduents_management.major.entity.Major;
import com.example.stduents_management.major.repository.MajorRepository;
import com.example.stduents_management.trainingprogram.dto.TrainingProgramRequest;
import com.example.stduents_management.trainingprogram.dto.TrainingProgramResponse;
import com.example.stduents_management.trainingprogram.entity.TrainingProgram;
import com.example.stduents_management.trainingprogram.repository.TrainingProgramRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrainingProgramService {

    private final TrainingProgramRepository trainingProgramRepository;
    private final MajorRepository majorRepository;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /* ================= SEARCH ================= */
    public Page<TrainingProgramResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("programName"));

        Page<TrainingProgram> programs;
        
        if (keyword == null || keyword.isBlank()) {
            programs = trainingProgramRepository.findAll(pageable);
        } else {
            // Tìm kiếm gần đúng theo mã, tên chương trình, khóa học
            programs = trainingProgramRepository
                    .findByProgramCodeContainingIgnoreCaseOrProgramNameContainingIgnoreCaseOrCourseContainingIgnoreCase(
                            keyword, keyword, keyword, pageable
                    );
        }

        return programs.map(p -> new TrainingProgramResponse(
                p.getProgramId(),
                p.getProgramCode(),
                p.getProgramName(),
                p.getMajor().getMajorId(),
                p.getMajor().getMajorName(),
                p.getCourse(),
                p.getDescription(),
                p.getDurationYears(),
                p.getTotalCredits(),
                p.getIsActive()
        ));
    }

    public TrainingProgramResponse getById(UUID id) {
        TrainingProgram p = trainingProgramRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy chương trình đào tạo"
                ));

        return new TrainingProgramResponse(
                p.getProgramId(),
                p.getProgramCode(),
                p.getProgramName(),
                p.getMajor().getMajorId(),
                p.getMajor().getMajorName(),
                p.getCourse(),
                p.getDescription(),
                p.getDurationYears(),
                p.getTotalCredits(),
                p.getIsActive()
        );
    }

    /* ================= CREATE ================= */
    @Transactional
    public TrainingProgramResponse create(TrainingProgramRequest req) {
        String code = normalize(req.getProgramCode());
        String name = normalize(req.getProgramName());
        String course = normalize(req.getCourse());

        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy ngành"
                ));

        // Kiểm tra mã chương trình đã tồn tại chưa (trong cùng ngành và khóa)
        if (trainingProgramRepository.existsByProgramCodeIgnoreCaseAndMajor_MajorIdAndCourseIgnoreCase(
                code, major.getMajorId(), course)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã chương trình đã tồn tại cho ngành và khóa này"
            );
        }

        TrainingProgram p = new TrainingProgram();
        p.setProgramCode(code);
        p.setProgramName(name);
        p.setMajor(major);
        p.setCourse(course);
        p.setDescription(normalize(req.getDescription()));
        p.setDurationYears(req.getDurationYears());
        p.setTotalCredits(req.getTotalCredits());
        p.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);

        trainingProgramRepository.save(p);

        return new TrainingProgramResponse(
                p.getProgramId(),
                p.getProgramCode(),
                p.getProgramName(),
                p.getMajor().getMajorId(),
                p.getMajor().getMajorName(),
                p.getCourse(),
                p.getDescription(),
                p.getDurationYears(),
                p.getTotalCredits(),
                p.getIsActive()
        );
    }

    /* ================= UPDATE ================= */
    @Transactional
    public TrainingProgramResponse update(UUID id, TrainingProgramRequest req) {
        TrainingProgram p = trainingProgramRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy chương trình đào tạo"
                ));

        String code = normalize(req.getProgramCode());
        String name = normalize(req.getProgramName());
        String course = normalize(req.getCourse());

        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy ngành"
                ));

        // Kiểm tra mã chương trình đã tồn tại chưa (trừ chính nó)
        if (trainingProgramRepository.existsByProgramCodeIgnoreCaseAndMajor_MajorIdAndCourseIgnoreCaseAndProgramIdNot(
                code, major.getMajorId(), course, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Mã chương trình đã tồn tại cho ngành và khóa này"
            );
        }

        p.setProgramCode(code);
        p.setProgramName(name);
        p.setMajor(major);
        p.setCourse(course);
        p.setDescription(normalize(req.getDescription()));
        p.setDurationYears(req.getDurationYears());
        p.setTotalCredits(req.getTotalCredits());
        p.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);

        trainingProgramRepository.save(p);

        return new TrainingProgramResponse(
                p.getProgramId(),
                p.getProgramCode(),
                p.getProgramName(),
                p.getMajor().getMajorId(),
                p.getMajor().getMajorName(),
                p.getCourse(),
                p.getDescription(),
                p.getDurationYears(),
                p.getTotalCredits(),
                p.getIsActive()
        );
    }

    /* ================= DELETE ================= */
    @Transactional
    public void delete(UUID id) {
        if (!trainingProgramRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy chương trình đào tạo"
            );
        }

        trainingProgramRepository.deleteById(id);
    }

    /* ================= EXPORT ================= */
    public void exportExcel(HttpServletResponse response) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Training Programs");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mã chương trình");
            header.createCell(1).setCellValue("Tên chương trình");
            header.createCell(2).setCellValue("Ngành");
            header.createCell(3).setCellValue("Khóa");
            header.createCell(4).setCellValue("Thời gian (năm)");
            header.createCell(5).setCellValue("Tổng tín chỉ");
            header.createCell(6).setCellValue("Mô tả");
            header.createCell(7).setCellValue("Trạng thái");

            List<TrainingProgram> programs = trainingProgramRepository.findAll(
                    Sort.by("programName")
            );

            int rowIdx = 1;
            for (TrainingProgram p : programs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getProgramCode());
                row.createCell(1).setCellValue(p.getProgramName());
                row.createCell(2).setCellValue(p.getMajor().getMajorName());
                row.createCell(3).setCellValue(p.getCourse());
                
                Cell durationCell = row.createCell(4);
                if (p.getDurationYears() != null) {
                    durationCell.setCellValue(p.getDurationYears());
                } else {
                    durationCell.setCellValue("");
                }
                
                Cell creditsCell = row.createCell(5);
                if (p.getTotalCredits() != null) {
                    creditsCell.setCellValue(p.getTotalCredits());
                } else {
                    creditsCell.setCellValue("");
                }
                
                row.createCell(6).setCellValue(
                        p.getDescription() != null ? p.getDescription() : ""
                );
                row.createCell(7).setCellValue(
                        p.getIsActive() != null && p.getIsActive() ? "Hoạt động" : "Không hoạt động"
                );
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=training_programs.xlsx"
            );

            wb.write(response.getOutputStream());
            response.flushBuffer();

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể export Excel"
            );
        }
    }

    /* ================= IMPORT ================= */
    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File rỗng"
            );
        }

        int count = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Cell codeCell = row.getCell(0);
                    Cell nameCell = row.getCell(1);
                    Cell majorCell = row.getCell(2);
                    Cell courseCell = row.getCell(3);

                    if (codeCell == null || nameCell == null || majorCell == null || courseCell == null) {
                        continue;
                    }

                    String code = getCellValueAsString(codeCell).trim();
                    String name = getCellValueAsString(nameCell).trim();
                    String majorName = getCellValueAsString(majorCell).trim();
                    String course = getCellValueAsString(courseCell).trim();

                    if (code.isEmpty() || name.isEmpty() || majorName.isEmpty() || course.isEmpty()) {
                        continue;
                    }

                    // Tìm ngành theo tên
                    Major major = majorRepository.findAll()
                            .stream()
                            .filter(m -> m.getMajorName().equalsIgnoreCase(majorName))
                            .findFirst()
                            .orElse(null);

                    if (major == null) continue;

                    // Kiểm tra xem đã tồn tại chưa
                    boolean exists = trainingProgramRepository.findAll()
                            .stream()
                            .anyMatch(tp -> tp.getProgramCode().equalsIgnoreCase(code)
                                    && tp.getMajor().getMajorId().equals(major.getMajorId())
                                    && tp.getCourse().equalsIgnoreCase(course));

                    if (exists) continue;

                    // Đọc các trường tùy chọn
                    Integer durationYears = null;
                    Cell durationCell = row.getCell(4);
                    if (durationCell != null) {
                        try {
                            String durationStr = getCellValueAsString(durationCell).trim();
                            if (!durationStr.isEmpty()) {
                                durationYears = Integer.parseInt(durationStr);
                            }
                        } catch (NumberFormatException e) {
                            // Bỏ qua nếu không parse được
                        }
                    }

                    Integer totalCredits = null;
                    Cell creditsCell = row.getCell(5);
                    if (creditsCell != null) {
                        try {
                            String creditsStr = getCellValueAsString(creditsCell).trim();
                            if (!creditsStr.isEmpty()) {
                                totalCredits = Integer.parseInt(creditsStr);
                            }
                        } catch (NumberFormatException e) {
                            // Bỏ qua nếu không parse được
                        }
                    }

                    String description = "";
                    Cell descCell = row.getCell(6);
                    if (descCell != null) {
                        description = getCellValueAsString(descCell).trim();
                    }

                    TrainingProgram p = new TrainingProgram();
                    p.setProgramCode(code);
                    p.setProgramName(name);
                    p.setMajor(major);
                    p.setCourse(course);
                    p.setDurationYears(durationYears);
                    p.setTotalCredits(totalCredits);
                    p.setDescription(description.isEmpty() ? null : description);
                    p.setIsActive(true);

                    trainingProgramRepository.save(p);
                    count++;
                } catch (Exception e) {
                    // Bỏ qua dòng lỗi và tiếp tục
                    continue;
                }
            }

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ"
            );
        }

        return count;
    }

    /* ================= PRINT ================= */
    public List<TrainingProgramResponse> getForPrint() {
        return trainingProgramRepository.findAll(Sort.by("programName"))
                .stream()
                .map(p -> new TrainingProgramResponse(
                        p.getProgramId(),
                        p.getProgramCode(),
                        p.getProgramName(),
                        p.getMajor().getMajorId(),
                        p.getMajor().getMajorName(),
                        p.getCourse(),
                        p.getDescription(),
                        p.getDurationYears(),
                        p.getTotalCredits(),
                        p.getIsActive()
                ))
                .toList();
    }
}

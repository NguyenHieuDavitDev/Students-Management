package com.example.stduents_management.classroom.service;

import com.example.stduents_management.classroom.dto.ClassRequest;
import com.example.stduents_management.classroom.dto.ClassResponse;
import com.example.stduents_management.classroom.entity.ClassEntity;
import com.example.stduents_management.classroom.repository.ClassRepository;
import com.example.stduents_management.major.entity.Major;
import com.example.stduents_management.major.repository.MajorRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final MajorRepository majorRepository;

    /* ===== SEARCH + PAGINATION ===== */
    public Page<ClassResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("className"));

        Page<ClassEntity> classes =
                (keyword == null || keyword.isBlank())
                        ? classRepository.findAll(pageable)
                        : classRepository
                        .findByClassNameContainingIgnoreCaseOrClassCodeContainingIgnoreCase(
                                keyword, keyword, pageable
                        );

        return classes.map(this::toResponse);
    }

    public ClassResponse getById(UUID id) {
        ClassEntity c = classRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lớp"
                ));
        return toResponse(c);
    }

    /* ===== CREATE ===== */
    @Transactional
    public ClassResponse create(ClassRequest req) {
        if (classRepository.existsByClassCodeIgnoreCaseAndAcademicYear(
                req.getClassCode(), req.getAcademicYear()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mã lớp đã tồn tại"
            );
        }

        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ngành không tồn tại"
                ));

        ClassEntity c = new ClassEntity();
        mapRequest(c, req, major);
        classRepository.save(c);

        return toResponse(c);
    }

    /* ===== UPDATE ===== */
    @Transactional
    public ClassResponse update(UUID id, ClassRequest req) {
        ClassEntity c = classRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lớp"
                ));

        if (classRepository.existsByClassCodeIgnoreCaseAndAcademicYearAndClassIdNot(
                req.getClassCode(), req.getAcademicYear(), id
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Mã lớp đã tồn tại"
            );
        }

        Major major = majorRepository.findById(req.getMajorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ngành không tồn tại"
                ));

        mapRequest(c, req, major);
        return toResponse(c);
    }

    @Transactional
    public void delete(UUID id) {
        if (!classRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Không tìm thấy lớp"
            );
        }
        classRepository.deleteById(id);
    }

    /* ===== EXPORT EXCEL ===== */
    public void exportExcel(HttpServletResponse response) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Classes");

            Row header = sheet.createRow(0);
            String[] titles = {
                    "Mã lớp", "Tên lớp", "Năm học", "Ngành",
                    "Hệ đào tạo", "Trình độ", "Sĩ số",
                    "Trạng thái", "Hoạt động"
            };

            for (int i = 0; i < titles.length; i++) {
                header.createCell(i).setCellValue(titles[i]);
            }

            List<ClassEntity> classes =
                    classRepository.findAll(Sort.by("className"));

            int rowIdx = 1;
            for (ClassEntity c : classes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getClassCode());
                row.createCell(1).setCellValue(c.getClassName());
                row.createCell(2).setCellValue(c.getAcademicYear());
                row.createCell(3).setCellValue(c.getMajor().getMajorName());
                row.createCell(4).setCellValue(c.getEducationType());
                row.createCell(5).setCellValue(c.getTrainingLevel());
                row.createCell(6).setCellValue(
                        c.getMaxStudent() == null ? 0 : c.getMaxStudent()
                );
                row.createCell(7).setCellValue(c.getClassStatus());
                row.createCell(8).setCellValue(
                        Boolean.TRUE.equals(c.getIsActive()) ? "Active" : "Inactive"
                );
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=classes.xlsx"
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

    /* ===== IMPORT EXCEL ===== */
    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File rỗng"
            );
        }

        Map<String, Major> majorMap =
                majorRepository.findAll()
                        .stream()
                        .collect(Collectors.toMap(
                                m -> m.getMajorName().toLowerCase(),
                                m -> m
                        ));

        int count = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String classCode = row.getCell(0).getStringCellValue().trim();
                String className = row.getCell(1).getStringCellValue().trim();
                String academicYear = row.getCell(2).getStringCellValue().trim();
                String majorName = row.getCell(3).getStringCellValue().trim().toLowerCase();

                Major major = majorMap.get(majorName);
                if (major == null) continue;

                if (classRepository.existsByClassCodeIgnoreCaseAndAcademicYear(
                        classCode, academicYear
                )) continue;

                ClassEntity c = new ClassEntity();
                c.setClassCode(classCode);
                c.setClassName(className);
                c.setAcademicYear(academicYear);
                c.setMajor(major);
                c.setEducationType(getString(row, 4));
                c.setTrainingLevel(getString(row, 5));
                c.setMaxStudent((int) getNumeric(row, 6));
                c.setClassStatus(getString(row, 7));
                c.setIsActive("active".equalsIgnoreCase(getString(row, 8)));

                classRepository.save(c);
                count++;
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ"
            );
        }

        return count;
    }

    /* ===== PRINT ===== */
    public List<ClassResponse> getForPrint() {
        return classRepository.findAll(Sort.by("className"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ===== UTIL ===== */
    private String getString(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? null : cell.getStringCellValue();
    }

    private double getNumeric(Row row, int index) {
        Cell cell = row.getCell(index);
        return cell == null ? 0 : cell.getNumericCellValue();
    }

    /* ===== MAPPER ===== */
    private void mapRequest(ClassEntity c, ClassRequest r, Major m) {
        c.setClassCode(r.getClassCode().trim());
        c.setClassName(r.getClassName().trim());
        c.setAcademicYear(r.getAcademicYear());
        c.setMajor(m);
        c.setEducationType(r.getEducationType());
        c.setTrainingLevel(r.getTrainingLevel());
        c.setMaxStudent(r.getMaxStudent());
        c.setClassStatus(r.getClassStatus());
        c.setIsActive(r.getIsActive() != null ? r.getIsActive() : true);
    }

    private ClassResponse toResponse(ClassEntity c) {
        return new ClassResponse(
                c.getClassId(),
                c.getClassCode(),
                c.getClassName(),
                c.getAcademicYear(),
                c.getMajor().getMajorId(),
                c.getMajor().getMajorName(),
                c.getEducationType(),
                c.getTrainingLevel(),
                c.getMaxStudent(),
                c.getClassStatus(),
                c.getIsActive()
        );
    }
}

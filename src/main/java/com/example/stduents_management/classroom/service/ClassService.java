package com.example.stduents_management.classroom.service;

import com.example.stduents_management.classroom.dto.ClassRequest;
import com.example.stduents_management.classroom.dto.ClassResponse;
import com.example.stduents_management.classroom.entity.ClassEntity;
import com.example.stduents_management.classroom.repository.ClassRepository;
import com.example.stduents_management.educationtype.entity.EducationType;
import com.example.stduents_management.educationtype.repository.EducationTypeRepository;
import com.example.stduents_management.major.entity.Major;
import com.example.stduents_management.major.repository.MajorRepository;
import com.example.stduents_management.traininglevel.entity.TrainingLevel;
import com.example.stduents_management.traininglevel.repository.TrainingLevelRepository;
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

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final MajorRepository majorRepository;
    private final EducationTypeRepository educationTypeRepository;
    private final TrainingLevelRepository trainingLevelRepository;

    /* ================= SEARCH ================= */
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

    /* ================= GET BY ID ================= */
    public ClassResponse getById(UUID id) {
        return classRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy lớp"
                ));
    }

    /* ================= CREATE ================= */
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

    /* ================= UPDATE ================= */
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

    /* ================= DELETE ================= */
    @Transactional
    public void delete(UUID id) {
        classRepository.deleteById(id);
    }

    /* ================= PRINT ================= */
    public List<ClassResponse> getForPrint() {
        return classRepository.findAll(Sort.by("className"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /* ================= EXPORT EXCEL ================= */
    public void exportExcel(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Classes");
            Row header = sheet.createRow(0);

            String[] columns = {
                    "Mã lớp", "Tên lớp", "Năm học",
                    "Ngành", "Hệ đào tạo", "Trình độ",
                    "Sĩ số tối đa", "Trạng thái"
            };

            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            List<ClassEntity> classes = classRepository.findAll();
            int rowIdx = 1;

            for (ClassEntity c : classes) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(c.getClassCode());
                row.createCell(1).setCellValue(c.getClassName());
                row.createCell(2).setCellValue(c.getAcademicYear());
                row.createCell(3).setCellValue(c.getMajor().getMajorName());
                row.createCell(4).setCellValue(c.getEducationType().getEducationTypeName());
                row.createCell(5).setCellValue(c.getTrainingLevel().getTrainingLevelName());
                row.createCell(6).setCellValue(
                        c.getMaxStudent() != null ? c.getMaxStudent() : 0
                );
                row.createCell(7).setCellValue(c.getClassStatus());
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=classes.xlsx"
            );

            workbook.write(response.getOutputStream());

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể export Excel"
            );
        }
    }

    /* ================= IMPORT EXCEL ================= */
    @Transactional
    public int importExcel(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            int count = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                ClassEntity c = new ClassEntity();

                c.setClassCode(row.getCell(0).getStringCellValue());
                c.setClassName(row.getCell(1).getStringCellValue());
                c.setAcademicYear(row.getCell(2).getStringCellValue());

                String majorName = row.getCell(3).getStringCellValue();
                Major major = majorRepository
                        .findByMajorName(majorName)
                        .orElse(null);
                if (major == null) continue;

                String eduName = row.getCell(4).getStringCellValue();
                EducationType edu = educationTypeRepository
                        .findByEducationTypeName(eduName)
                        .orElse(null);
                if (edu == null) continue;

                String levelName = row.getCell(5).getStringCellValue();
                TrainingLevel level = trainingLevelRepository
                        .findByTrainingLevelName(levelName)
                        .orElse(null);
                if (level == null) continue;

                c.setMajor(major);
                c.setEducationType(edu);
                c.setTrainingLevel(level);
                c.setMaxStudent((int) row.getCell(6).getNumericCellValue());
                c.setClassStatus(row.getCell(7).getStringCellValue());
                c.setIsActive(true);

                classRepository.save(c);
                count++;
            }

            return count;

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File Excel không hợp lệ"
            );
        }
    }

    /* ================= MAPPER ================= */
    private void mapRequest(ClassEntity c, ClassRequest r, Major m) {

        EducationType edu =
                educationTypeRepository.findById(r.getEducationTypeId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Hệ đào tạo không tồn tại"
                        ));

        TrainingLevel level =
                trainingLevelRepository.findById(r.getTrainingLevelId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Trình độ đào tạo không tồn tại"
                        ));

        c.setClassCode(r.getClassCode().trim());
        c.setClassName(r.getClassName().trim());
        c.setAcademicYear(r.getAcademicYear());
        c.setMajor(m);
        c.setEducationType(edu);
        c.setTrainingLevel(level);
        c.setMaxStudent(r.getMaxStudent());
        c.setClassStatus(r.getClassStatus());
        c.setIsActive(
                r.getIsActive() != null ? r.getIsActive() : true
        );
    }

    private ClassResponse toResponse(ClassEntity c) {
        return new ClassResponse(
                c.getClassId(),
                c.getClassCode(),
                c.getClassName(),
                c.getAcademicYear(),

                c.getMajor() != null ? c.getMajor().getMajorId() : null,
                c.getMajor() != null ? c.getMajor().getMajorName() : null,

                c.getEducationType() != null ? c.getEducationType().getEducationTypeId() : null,
                c.getEducationType() != null ? c.getEducationType().getEducationTypeName() : null,

                c.getTrainingLevel() != null ? c.getTrainingLevel().getTrainingLevelId() : null,
                c.getTrainingLevel() != null ? c.getTrainingLevel().getTrainingLevelName() : null,

                c.getMaxStudent(),
                c.getClassStatus(),
                c.getIsActive()
        );
    }
}

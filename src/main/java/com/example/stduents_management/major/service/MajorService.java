package com.example.stduents_management.major.service;

import com.example.stduents_management.faculty.entity.Faculty;
import com.example.stduents_management.faculty.repository.FacultyRepository;
import com.example.stduents_management.major.dto.MajorRequest;
import com.example.stduents_management.major.dto.MajorResponse;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;
    private final FacultyRepository facultyRepository;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    /* ===== SEARCH ===== */
    public Page<MajorResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("majorName"));

        Page<Major> majors =
                (keyword == null || keyword.isBlank())
                        ? majorRepository.findAll(pageable)
                        : majorRepository.findByMajorNameContainingIgnoreCase(keyword, pageable);

        return majors.map(m ->
                new MajorResponse(
                        m.getMajorId(),
                        m.getMajorName(),
                        m.getFaculty().getFacultyId(),
                        m.getFaculty().getFacultyName()
                )
        );
    }

    public MajorResponse getById(UUID id) {
        Major m = majorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy ngành"
                ));

        return new MajorResponse(
                m.getMajorId(),
                m.getMajorName(),
                m.getFaculty().getFacultyId(),
                m.getFaculty().getFacultyName()
        );
    }

    /* ===== CREATE ===== */
    @Transactional
    public MajorResponse create(MajorRequest req) {
        String name = normalize(req.getMajorName());

        Faculty faculty = facultyRepository.findById(req.getFacultyId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Khoa không tồn tại"
                ));

        if (majorRepository.existsByMajorNameIgnoreCaseAndFaculty_FacultyId(
                name, faculty.getFacultyId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ngành đã tồn tại trong khoa này"
            );
        }

        Major m = new Major();
        m.setMajorName(name);
        m.setFaculty(faculty);
        majorRepository.save(m);

        return new MajorResponse(
                m.getMajorId(),
                m.getMajorName(),
                faculty.getFacultyId(),
                faculty.getFacultyName()
        );
    }

    /* ===== UPDATE ===== */
    @Transactional
    public MajorResponse update(UUID id, MajorRequest req) {
        Major m = majorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy ngành"
                ));

        String name = normalize(req.getMajorName());

        Faculty faculty = facultyRepository.findById(req.getFacultyId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Khoa không tồn tại"
                ));

        if (majorRepository.existsByMajorNameIgnoreCaseAndFaculty_FacultyIdAndMajorIdNot(
                name, faculty.getFacultyId(), id
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ngành đã tồn tại trong khoa này"
            );
        }

        m.setMajorName(name);
        m.setFaculty(faculty);
        majorRepository.save(m);

        return new MajorResponse(
                m.getMajorId(),
                m.getMajorName(),
                faculty.getFacultyId(),
                faculty.getFacultyName()
        );
    }

    @Transactional
    public void delete(UUID id) {
        if (!majorRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Không tìm thấy ngành"
            );
        }
        majorRepository.deleteById(id);
    }

    /* ===== EXPORT ===== */
    public void exportExcel(HttpServletResponse response) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Majors");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Tên ngành");
            header.createCell(1).setCellValue("Khoa");

            List<Major> majors = majorRepository.findAll(Sort.by("majorName"));

            int rowIdx = 1;
            for (Major m : majors) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(m.getMajorName());
                row.createCell(1).setCellValue(m.getFaculty().getFacultyName());
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=majors.xlsx"
            );

            wb.write(response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Không thể export Excel"
            );
        }
    }

    /* ===== IMPORT ===== */
    @Transactional
    public int importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File rỗng"
            );
        }

        int count = 0;

        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String majorName = row.getCell(0).getStringCellValue().trim();
                String facultyName = row.getCell(1).getStringCellValue().trim();

                Faculty faculty = facultyRepository
                        .findAll()
                        .stream()
                        .filter(f -> f.getFacultyName().equalsIgnoreCase(facultyName))
                        .findFirst()
                        .orElse(null);

                if (faculty == null) continue;

                if (majorRepository.existsByMajorNameIgnoreCaseAndFaculty_FacultyId(
                        majorName, faculty.getFacultyId()
                )) continue;

                Major m = new Major();
                m.setMajorName(majorName);
                m.setFaculty(faculty);
                majorRepository.save(m);
                count++;
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File Excel không hợp lệ"
            );
        }

        return count;
    }

    public List<MajorResponse> getForPrint() {
        return majorRepository.findAll(Sort.by("majorName"))
                .stream()
                .map(m -> new MajorResponse(
                        m.getMajorId(),
                        m.getMajorName(),
                        m.getFaculty().getFacultyId(),
                        m.getFaculty().getFacultyName()
                ))
                .toList();
    }
}

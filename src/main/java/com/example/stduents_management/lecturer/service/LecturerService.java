package com.example.stduents_management.lecturer.service;

import com.example.stduents_management.common.service.FileStorageService;
import com.example.stduents_management.department.entity.Department;
import com.example.stduents_management.department.repository.DepartmentRepository;
import com.example.stduents_management.faculty.entity.Faculty;
import com.example.stduents_management.faculty.repository.FacultyRepository;
import com.example.stduents_management.lecturer.dto.LecturerRequest;
import com.example.stduents_management.lecturer.dto.LecturerResponse;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.lecturerduty.entity.LecturerDuty;
import com.example.stduents_management.lecturerduty.repository.LecturerDutyRepository;
import com.example.stduents_management.position.entity.Position;
import com.example.stduents_management.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LecturerService {

    private final LecturerRepository lecturerRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final LecturerDutyRepository lecturerDutyRepository;
    private final FileStorageService fileStorageService;

    public Page<LecturerResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lecturerCode"));
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Lecturer> data = lecturerRepository.pageTeachingLecturers(kw, pageable);
        return data.map(this::toResponse);
    }

    public LecturerResponse getById(UUID id) {
        return lecturerRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên"));
    }

    @Transactional
    public void create(LecturerRequest req) {
        String lecturerCode = normalize(req.getLecturerCode());
        if (lecturerRepository.existsByLecturerCodeIgnoreCase(lecturerCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã giảng viên đã tồn tại");
        }
        req.setLecturerCode(lecturerCode);
        Lecturer l = build(new Lecturer(), req);
        lecturerRepository.save(l);
    }

    @Transactional
    public void update(UUID id, LecturerRequest req) {
        Lecturer l = lecturerRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên"));

        String lecturerCode = normalize(req.getLecturerCode());
        if (lecturerRepository.existsByLecturerCodeIgnoreCaseAndLecturerIdNot(lecturerCode, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã giảng viên đã tồn tại");
        }
        req.setLecturerCode(lecturerCode);

        build(l, req);
    }

    @Transactional
    public void delete(UUID id) {
        if (!lecturerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên");
        }
        try {
            lecturerRepository.deleteById(id);
            lecturerRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Không thể xóa giảng viên vì đang được sử dụng ở dữ liệu khác"
            );
        }
    }

    public List<LecturerResponse> getForPrint() {
        return lecturerRepository.findTeachingLecturersOrderByCode()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Lecturer build(Lecturer l, LecturerRequest req) {

        Faculty faculty = facultyRepository.findById(req.getFacultyId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Khoa không tồn tại"));

        Position position = null;
        if (req.getPositionId() != null) {
            position = positionRepository.findById(req.getPositionId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Học vị không tồn tại"));
        }

        LecturerDuty lecturerDuty = null;
        if (req.getLecturerDutyId() != null) {
            lecturerDuty = lecturerDutyRepository.findById(req.getLecturerDutyId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Chức vụ không tồn tại"));
        }

        Department department = null;
        if (req.getDepartmentId() != null) {
            department = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Phòng ban không tồn tại"));
        }

        l.setLecturerCode(normalize(req.getLecturerCode()));
        l.setFullName(normalize(req.getFullName()));
        l.setDateOfBirth(req.getDateOfBirth());
        l.setGender(normalize(req.getGender()));
        l.setCitizenId(normalize(req.getCitizenId()));
        l.setEmail(normalize(req.getEmail()));
        l.setPhoneNumber(normalize(req.getPhoneNumber()));
        l.setAddress(normalize(req.getAddress()));
        l.setPosition(position);
        l.setLecturerDuty(lecturerDuty);
        l.setDepartment(department);
        l.setAcademicTitle(normalize(req.getAcademicTitle()));

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            l.setAvatar(fileStorageService.store(req.getAvatarFile()));
        }

        l.setFaculty(faculty);
        return l;
    }

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    private LecturerResponse toResponse(Lecturer l) {
        return new LecturerResponse(
                l.getLecturerId(),
                l.getLecturerCode(),
                l.getFullName(),
                l.getDateOfBirth(),
                l.getGender(),
                l.getCitizenId(),
                l.getEmail(),
                l.getPhoneNumber(),
                l.getAddress(),
                l.getAvatar(),
                l.getPosition() != null ? l.getPosition().getPositionId() : null,
                l.getPosition() != null ? l.getPosition().getPositionName() : null,
                l.getAcademicTitle(),
                l.getLecturerDuty() != null ? l.getLecturerDuty().getLecturerDutyId() : null,
                l.getLecturerDuty() != null ? l.getLecturerDuty().getDutyName() : null,
                l.getFaculty().getFacultyId(),
                l.getFaculty().getFacultyName(),
                l.getDepartment() != null ? l.getDepartment().getDepartmentId() : null,
                l.getDepartment() != null ? l.getDepartment().getDepartmentName() : null
        );
    }
}

package com.example.stduents_management.lecturer.service;

import com.example.stduents_management.common.service.FileStorageService;
import com.example.stduents_management.faculty.entity.Faculty;
import com.example.stduents_management.faculty.repository.FacultyRepository;
import com.example.stduents_management.lecturer.dto.LecturerRequest;
import com.example.stduents_management.lecturer.dto.LecturerResponse;
import com.example.stduents_management.lecturer.entity.Lecturer;
import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.position.entity.Position;
import com.example.stduents_management.position.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
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
    private final PositionRepository positionRepository;
    private final FileStorageService fileStorageService;

    public Page<LecturerResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lecturerCode"));

        Page<Lecturer> data = (keyword == null || keyword.isBlank())
                ? lecturerRepository.findAll(pageable)
                : lecturerRepository
                .findByLecturerCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                        keyword, keyword, pageable
                );

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
        if (lecturerRepository.existsByLecturerCode(req.getLecturerCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã giảng viên đã tồn tại");
        }
        lecturerRepository.save(build(new Lecturer(), req));
    }

    @Transactional
    public void update(UUID id, LecturerRequest req) {
        Lecturer l = lecturerRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy giảng viên"));

        if (lecturerRepository.existsByLecturerCodeAndLecturerIdNot(req.getLecturerCode(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã giảng viên đã tồn tại");
        }

        build(l, req);
    }

    @Transactional
    public void delete(UUID id) {
        lecturerRepository.deleteById(id);
    }

    public List<LecturerResponse> getForPrint() {
        return lecturerRepository.findAll(Sort.by("lecturerCode"))
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
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Chức danh không tồn tại"));
        }

        l.setLecturerCode(req.getLecturerCode());
        l.setFullName(req.getFullName());
        l.setDateOfBirth(req.getDateOfBirth());
        l.setGender(req.getGender());
        l.setCitizenId(req.getCitizenId());
        l.setEmail(req.getEmail());
        l.setPhoneNumber(req.getPhoneNumber());
        l.setAddress(req.getAddress());
        l.setPosition(position);
        l.setAcademicTitle(req.getAcademicTitle());

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            l.setAvatar(fileStorageService.store(req.getAvatarFile()));
        }

        l.setFaculty(faculty);
        return l;
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
                l.getFaculty().getFacultyId(),
                l.getFaculty().getFacultyName()
        );
    }
}

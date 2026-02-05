package com.example.stduents_management.faculty.service;

import com.example.stduents_management.faculty.dto.FacultyRequest;
import com.example.stduents_management.faculty.dto.FacultyResponse;
import com.example.stduents_management.faculty.entity.Faculty;
import com.example.stduents_management.faculty.repository.FacultyRepository;
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
public class FacultyService {

    private final FacultyRepository facultyRepository;

    private String normalize(String s) {
        return s == null ? null : s.trim();
    }

    public List<FacultyResponse> getAll() {
        return facultyRepository.findAll(Sort.by("facultyName"))
                .stream()
                .map(f -> new FacultyResponse(
                        f.getFacultyId(),
                        f.getFacultyCode(),
                        f.getFacultyName()
                ))
                .toList();
    }

    // tìm kiếm + phân trang
    public Page<FacultyResponse> search(
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("facultyName").ascending()
        );

        Page<Faculty> faculties;

        if (keyword == null || keyword.trim().isEmpty()) {
            faculties = facultyRepository.findAll(pageable);
        } else {
            faculties = facultyRepository
                    .findByFacultyCodeContainingIgnoreCaseOrFacultyNameContainingIgnoreCase(
                            keyword,
                            keyword,
                            pageable
                    );
        }

        return faculties.map(f ->
                new FacultyResponse(
                        f.getFacultyId(),
                        f.getFacultyCode(),
                        f.getFacultyName()
                )
        );
    }

    public FacultyResponse getById(UUID id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Faculty not found"
                        )
                );

        return new FacultyResponse(
                faculty.getFacultyId(),
                faculty.getFacultyCode(),
                faculty.getFacultyName()
        );
    }

    @Transactional
    public FacultyResponse create(FacultyRequest request) {
        String code = normalize(request.getFacultyCode());
        String name = normalize(request.getFacultyName());

        if (facultyRepository.existsByFacultyCode(code)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Faculty code already exists"
            );
        }

        Faculty faculty = new Faculty();
        faculty.setFacultyCode(code);
        faculty.setFacultyName(name);

        facultyRepository.save(faculty);

        return new FacultyResponse(
                faculty.getFacultyId(),
                faculty.getFacultyCode(),
                faculty.getFacultyName()
        );
    }

    @Transactional
    public FacultyResponse update(UUID id, FacultyRequest request) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Faculty not found"
                        )
                );

        String code = normalize(request.getFacultyCode());
        String name = normalize(request.getFacultyName());

        if (facultyRepository.existsByFacultyCodeAndFacultyIdNot(code, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Faculty code already exists"
            );
        }

        faculty.setFacultyCode(code);
        faculty.setFacultyName(name);

        facultyRepository.save(faculty);

        return new FacultyResponse(
                faculty.getFacultyId(),
                faculty.getFacultyCode(),
                faculty.getFacultyName()
        );
    }

    @Transactional
    public void delete(UUID id) {
        if (!facultyRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Faculty not found"
            );
        }
        facultyRepository.deleteById(id);
    }
}

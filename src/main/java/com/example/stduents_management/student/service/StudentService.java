package com.example.stduents_management.student.service;

import com.example.stduents_management.classroom.entity.ClassEntity;
import com.example.stduents_management.classroom.repository.ClassRepository;
import com.example.stduents_management.common.service.FileStorageService;
import com.example.stduents_management.student.dto.StudentRequest;
import com.example.stduents_management.student.dto.StudentResponse;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.student.repository.StudentRepository;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;
    private final FileStorageService fileStorageService;

    public Page<StudentResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("studentCode"));

        Page<Student> pageData =
                (keyword == null || keyword.isBlank())
                        ? studentRepository.findAll(pageable)
                        : studentRepository
                        .findByStudentCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                                keyword, keyword, pageable
                        );

        return pageData.map(this::toResponse);
    }

    public StudentResponse getById(UUID id) {
        return studentRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên")
                );
    }

    @Transactional
    public void create(StudentRequest req) {
        if (studentRepository.existsByStudentCode(req.getStudentCode())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã sinh viên đã tồn tại");
        }
        studentRepository.save(buildStudent(new Student(), req));
    }

    @Transactional
    public void update(UUID id, StudentRequest req) {
        Student s = studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên")
                );

        if (studentRepository.existsByStudentCodeAndStudentIdNot(
                req.getStudentCode(), id
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mã sinh viên đã tồn tại");
        }

        buildStudent(s, req);
    }

    @Transactional
    public void delete(UUID id) {
        if (!studentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sinh viên");
        }
        studentRepository.deleteById(id);
    }

    public List<StudentResponse> getForPrint() {
        return studentRepository.findAll(Sort.by("studentCode"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Student buildStudent(Student s, StudentRequest req) {

        ClassEntity clazz = classRepository.findById(req.getClassId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Lớp không tồn tại")
                );

        s.setStudentCode(req.getStudentCode().trim());
        s.setFullName(req.getFullName().trim());
        s.setDateOfBirth(req.getDateOfBirth());
        s.setGender(req.getGender());
        s.setCitizenId(req.getCitizenId());
        s.setEmail(req.getEmail());
        s.setPhoneNumber(req.getPhoneNumber());
        s.setAddress(req.getAddress());

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            s.setAvatar(fileStorageService.store(req.getAvatarFile()));
        }

        s.setClazz(clazz);
        return s;
    }

    private StudentResponse toResponse(Student s) {
        return new StudentResponse(
                s.getStudentId(),
                s.getStudentCode(),
                s.getFullName(),
                s.getDateOfBirth(),
                s.getGender(),
                s.getCitizenId(),
                s.getEmail(),
                s.getPhoneNumber(),
                s.getAddress(),
                s.getAvatar(),
                s.getClazz().getClassId(),
                s.getClazz().getClassName(),
                s.getClazz().getMajor().getMajorId(),
                s.getClazz().getMajor().getMajorName()
        );
    }
}

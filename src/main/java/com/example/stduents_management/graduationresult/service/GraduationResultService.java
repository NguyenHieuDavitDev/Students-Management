package com.example.stduents_management.graduationresult.service;

import com.example.stduents_management.graduationresult.dto.GraduationResultRequest;
import com.example.stduents_management.graduationresult.dto.GraduationResultResponse;
import com.example.stduents_management.graduationresult.entity.GraduationResult;
import com.example.stduents_management.graduationresult.entity.GraduationResultStatus;
import com.example.stduents_management.graduationresult.repository.GraduationResultRepository;
import com.example.stduents_management.student.entity.Student;
import com.example.stduents_management.student.repository.StudentRepository;
import com.example.stduents_management.trainingprogram.entity.TrainingProgram;
import com.example.stduents_management.trainingprogram.repository.TrainingProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GraduationResultService {

    private final GraduationResultRepository graduationResultRepository;
    private final StudentRepository studentRepository;
    private final TrainingProgramRepository trainingProgramRepository;

    public Page<GraduationResultResponse> search(String keyword, String statusStr, int page, int size) {
        GraduationResultStatus status = parseStatus(statusStr);
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size);
        return graduationResultRepository.search(term, status, pageable).map(this::toResponse);
    }

    public GraduationResultResponse getById(Long id) {
        return graduationResultRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy kết quả xét tốt nghiệp"));
    }

    public List<GraduationResultResponse> getAll() {
        return graduationResultRepository.findAllOrdered().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void create(GraduationResultRequest req) {
        Student student = resolveStudent(req.getStudentId());
        TrainingProgram program = resolveProgram(req.getProgramId());

        if (graduationResultRepository.findByStudent_StudentIdAndProgram_ProgramId(
                student.getStudentId(), program.getProgramId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Sinh viên này đã có kết quả xét tốt nghiệp cho chương trình đã chọn");
        }

        GraduationResult entity = new GraduationResult();
        buildEntity(entity, req, student, program);
        graduationResultRepository.save(entity);
    }

    @Transactional
    public void update(Long id, GraduationResultRequest req) {
        GraduationResult entity = graduationResultRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy kết quả xét tốt nghiệp"));

        Student student = resolveStudent(req.getStudentId());
        TrainingProgram program = resolveProgram(req.getProgramId());

        if (graduationResultRepository.existsByStudent_StudentIdAndProgram_ProgramIdAndIdNot(
                student.getStudentId(), program.getProgramId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Đã tồn tại kết quả khác cho cùng sinh viên và chương trình");
        }

        buildEntity(entity, req, student, program);
        graduationResultRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!graduationResultRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Không tìm thấy kết quả xét tốt nghiệp");
        }
        graduationResultRepository.deleteById(id);
    }

    private Student resolveStudent(UUID studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy sinh viên"));
    }

    private TrainingProgram resolveProgram(UUID programId) {
        return trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy chương trình đào tạo"));
    }

    private void buildEntity(GraduationResult entity, GraduationResultRequest req, Student student, TrainingProgram program) {
        entity.setStudent(student);
        entity.setProgram(program);
        entity.setTotalCredits(req.getTotalCredits());
        entity.setGpa(req.getGpa());
        entity.setCertificates(req.getCertificates() != null && !req.getCertificates().isBlank()
                ? req.getCertificates().trim() : null);
        entity.setMissingCourses(req.getMissingCourses() != null && !req.getMissingCourses().isBlank()
                ? req.getMissingCourses().trim() : null);
        entity.setNote(req.getNote() != null && !req.getNote().isBlank()
                ? req.getNote().trim() : null);
        entity.setStatus(req.getStatus());
    }

    private GraduationResultResponse toResponse(GraduationResult r) {
        Student s = r.getStudent();
        TrainingProgram p = r.getProgram();
        return new GraduationResultResponse(
                r.getId(),
                s.getStudentId(),
                s.getStudentCode(),
                s.getFullName(),
                p.getProgramId(),
                p.getProgramCode(),
                p.getProgramName(),
                p.getMajor() != null ? p.getMajor().getMajorName() : "",
                p.getCourse(),
                r.getTotalCredits(),
                r.getGpa(),
                r.getCertificates(),
                r.getMissingCourses(),
                r.getNote(),
                r.getStatus(),
                r.getStatus().getLabel(),
                r.getCheckedAt(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }

    private GraduationResultStatus parseStatus(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return GraduationResultStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}


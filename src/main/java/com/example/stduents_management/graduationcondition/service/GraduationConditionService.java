package com.example.stduents_management.graduationcondition.service;

import com.example.stduents_management.graduationcondition.dto.GraduationConditionRequest;
import com.example.stduents_management.graduationcondition.dto.GraduationConditionResponse;
import com.example.stduents_management.graduationcondition.entity.GraduationCondition;
import com.example.stduents_management.graduationcondition.repository.GraduationConditionRepository;
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
public class GraduationConditionService {

    private final GraduationConditionRepository graduationConditionRepository;
    private final TrainingProgramRepository trainingProgramRepository;

    public Page<GraduationConditionResponse> search(String keyword, int page, int size) {
        String term = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size);
        return graduationConditionRepository.search(term, pageable).map(this::toResponse);
    }

    public GraduationConditionResponse getById(Long id) {
        return graduationConditionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy điều kiện xét tốt nghiệp"));
    }

    public List<GraduationConditionResponse> getAll() {
        return graduationConditionRepository.findAllOrdered().stream().map(this::toResponse).toList();
    }

    /** Lấy điều kiện theo chương trình (dùng cho kiểm tra tốt nghiệp sau này). */
    public GraduationConditionResponse getByProgramId(UUID programId) {
        return graduationConditionRepository.findByProgram_ProgramId(programId)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public void create(GraduationConditionRequest req) {
        TrainingProgram program = resolveProgram(req.getProgramId());
        if (graduationConditionRepository.findByProgram_ProgramId(program.getProgramId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Chương trình đào tạo này đã có điều kiện xét tốt nghiệp. Chỉnh sửa bản ghi hiện có.");
        }
        GraduationCondition entity = new GraduationCondition();
        buildEntity(entity, req, program);
        graduationConditionRepository.save(entity);
    }

    @Transactional
    public void update(Long id, GraduationConditionRequest req) {
        GraduationCondition entity = graduationConditionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy điều kiện xét tốt nghiệp"));
        TrainingProgram program = resolveProgram(req.getProgramId());
        if (graduationConditionRepository.existsByProgram_ProgramIdAndIdNot(program.getProgramId(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Chương trình đào tạo này đã có điều kiện xét tốt nghiệp khác.");
        }
        buildEntity(entity, req, program);
        graduationConditionRepository.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        if (!graduationConditionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy điều kiện xét tốt nghiệp");
        }
        graduationConditionRepository.deleteById(id);
    }

    private TrainingProgram resolveProgram(UUID programId) {
        return trainingProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy chương trình đào tạo"));
    }

    private void buildEntity(GraduationCondition entity, GraduationConditionRequest req, TrainingProgram program) {
        entity.setProgram(program);
        entity.setMinCredits(req.getMinCredits());
        entity.setMinGpa(req.getMinGpa());
        entity.setRequiredCertificate(req.getRequiredCertificate() != null && !req.getRequiredCertificate().isBlank()
                ? req.getRequiredCertificate().trim() : null);
        entity.setRequiredCourses(req.getRequiredCourses() != null && !req.getRequiredCourses().isBlank()
                ? req.getRequiredCourses().trim() : null);
    }

    private GraduationConditionResponse toResponse(GraduationCondition g) {
        TrainingProgram p = g.getProgram();
        return new GraduationConditionResponse(
                g.getId(),
                p.getProgramId(),
                p.getProgramCode(),
                p.getProgramName(),
                p.getMajor() != null ? p.getMajor().getMajorName() : "",
                p.getCourse(),
                g.getMinCredits(),
                g.getMinGpa(),
                g.getRequiredCertificate(),
                g.getRequiredCourses(),
                g.getCreatedAt(),
                g.getUpdatedAt()
        );
    }
}

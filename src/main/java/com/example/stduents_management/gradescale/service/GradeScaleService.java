package com.example.stduents_management.gradescale.service;

import com.example.stduents_management.gradescale.dto.GradeScaleRequest;
import com.example.stduents_management.gradescale.dto.GradeScaleResponse;
import com.example.stduents_management.gradescale.entity.GradeScale;
import com.example.stduents_management.gradescale.repository.GradeScaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Quản lý bảng thang điểm (grade_scales).
 * Cung cấp CRUD và phương thức quy đổi điểm số → điểm chữ + GPA (Bước 4).
 *
 * Lưu ý: Việc tính điểm tổng kết (Bước 3) được thực hiện trong GradeTranscriptService.
 */
@Service
@RequiredArgsConstructor
public class GradeScaleService {

    private final GradeScaleRepository gradeScaleRepository;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public Page<GradeScaleResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "minScore"));
        String searchTerm = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return gradeScaleRepository.searchByKeyword(searchTerm, pageable).map(this::toResponse);
    }

    public GradeScaleResponse getById(UUID id) {
        return gradeScaleRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy thang điểm"));
    }

    @Transactional
    public void create(GradeScaleRequest req) {
        validate(req, null);
        GradeScale entity = new GradeScale();
        buildEntity(entity, req);
        gradeScaleRepository.save(entity);
    }

    @Transactional
    public void update(UUID id, GradeScaleRequest req) {
        GradeScale entity = gradeScaleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy thang điểm"));
        validate(req, id);
        buildEntity(entity, req);
        gradeScaleRepository.save(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!gradeScaleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thang điểm");
        }
        gradeScaleRepository.deleteById(id);
    }

    public List<GradeScaleResponse> getAll() {
        return gradeScaleRepository.findAll(Sort.by(Sort.Direction.DESC, "minScore"))
                .stream().map(this::toResponse).toList();
    }

    // ─── QUY ĐỔI ĐIỂM (Bước 4) ───────────────────────────────────────────────

    /**
     * Tra bảng grade_scales để quy đổi điểm số → điểm chữ + GPA + xếp loại.
     * Được gọi từ GradeTranscriptService sau khi tính xong điểm tổng kết.
     *
     * @param score Điểm hệ 10 (0.00 – 10.00)
     * @return GradeScaleResponse chứa letterGrade, gradePoint, description; empty nếu không tìm thấy
     */
    public Optional<GradeScaleResponse> convert(BigDecimal score) {
        if (score == null) return Optional.empty();
        return gradeScaleRepository.findByScore(score).map(this::toResponse);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void validate(GradeScaleRequest req, UUID excludeId) {
        if (req.getMinScore().compareTo(req.getMaxScore()) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Điểm tối thiểu phải nhỏ hơn điểm tối đa");
        }
        String letter = req.getLetterGrade().toUpperCase().trim();
        boolean letterExists = excludeId == null
                ? gradeScaleRepository.existsByLetterGrade(letter)
                : gradeScaleRepository.existsByLetterGradeAndIdNot(letter, excludeId);
        if (letterExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Điểm chữ '" + letter + "' đã tồn tại trong thang điểm");
        }
    }

    private void buildEntity(GradeScale entity, GradeScaleRequest req) {
        entity.setMinScore(req.getMinScore());
        entity.setMaxScore(req.getMaxScore());
        entity.setLetterGrade(req.getLetterGrade().toUpperCase().trim());
        entity.setGradePoint(req.getGradePoint());
        entity.setDescription(req.getDescription());
    }

    public GradeScaleResponse toResponse(GradeScale g) {
        return new GradeScaleResponse(
                g.getId(),
                g.getMinScore(),
                g.getMaxScore(),
                g.getLetterGrade(),
                g.getGradePoint(),
                g.getDescription()
        );
    }
}

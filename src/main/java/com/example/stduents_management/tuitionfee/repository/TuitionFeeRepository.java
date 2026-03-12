package com.example.stduents_management.tuitionfee.repository;

import com.example.stduents_management.tuitionfee.entity.TuitionFee;
import com.example.stduents_management.tuitionfee.entity.TuitionFeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TuitionFeeRepository extends JpaRepository<TuitionFee, UUID> {

    /**
     * Tìm kiếm gần đúng theo tên/mã chương trình đào tạo, ghi chú, hoặc trạng thái.
     * Sắp xếp theo ngày áp dụng giảm dần (mới nhất lên đầu).
     */
    @Query("""
        SELECT t FROM TuitionFee t
        JOIN t.trainingProgram p
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(p.programName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(p.programCode)  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(t.note)         LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR t.status = :status)
        ORDER BY t.effectiveDate DESC, p.programName ASC
        """)
    Page<TuitionFee> search(
            @Param("keyword") String keyword,
            @Param("status")  TuitionFeeStatus status,
            Pageable pageable
    );

    /** Tất cả bản ghi, sắp xếp theo ngày giảm dần (dùng cho print / export). */
    @Query("""
        SELECT t FROM TuitionFee t
        JOIN FETCH t.trainingProgram p
        ORDER BY t.effectiveDate DESC, p.programName ASC
        """)
    List<TuitionFee> findAllOrdered();

    /** Lấy mức học phí ACTIVE mới nhất của một chương trình đào tạo. */
    @Query("""
        SELECT t FROM TuitionFee t
        WHERE t.trainingProgram.programId = :programId
          AND t.status = 'ACTIVE'
        ORDER BY t.effectiveDate DESC
        """)
    Optional<TuitionFee> findActiveByProgram(@Param("programId") UUID programId);

    /** Kiểm tra có bản ghi ACTIVE khác (trừ chính nó) cho cùng chương trình không. */
    @Query("""
        SELECT COUNT(t) > 0 FROM TuitionFee t
        WHERE t.trainingProgram.programId = :programId
          AND t.status = 'ACTIVE'
          AND (:excludeId IS NULL OR t.id <> :excludeId)
        """)
    boolean existsActiveForProgram(
            @Param("programId")  UUID programId,
            @Param("excludeId")  UUID excludeId
    );

    /** Danh sách mức học phí theo chương trình (để hiển thị lịch sử). */
    @Query("""
        SELECT t FROM TuitionFee t
        WHERE t.trainingProgram.programId = :programId
        ORDER BY t.effectiveDate DESC
        """)
    List<TuitionFee> findByProgramId(@Param("programId") UUID programId);
}

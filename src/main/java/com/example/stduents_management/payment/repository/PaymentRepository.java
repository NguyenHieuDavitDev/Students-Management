package com.example.stduents_management.payment.repository;

import com.example.stduents_management.payment.entity.Payment;
import com.example.stduents_management.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
            SELECT p
            FROM Payment p
            JOIN p.studentTuition st
            JOIN st.student s
            JOIN st.semester sem
            WHERE (:status IS NULL OR p.status = :status)
              AND (
                    :keyword IS NULL OR :keyword = ''
                 OR LOWER(p.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(s.studentCode)           LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(s.fullName)              LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(sem.code)                LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(sem.name)                LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            ORDER BY p.paymentDate DESC, p.id DESC
            """)
    Page<Payment> search(
            @Param("keyword") String keyword,
            @Param("status") PaymentStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Payment p
            JOIN FETCH p.studentTuition st
            JOIN FETCH st.student s
            JOIN FETCH st.semester sem
            ORDER BY p.paymentDate DESC, p.id DESC
            """)
    List<Payment> findAllOrdered();

    List<Payment> findByStudentTuition_IdOrderByPaymentDateDesc(java.util.UUID studentTuitionId);
}

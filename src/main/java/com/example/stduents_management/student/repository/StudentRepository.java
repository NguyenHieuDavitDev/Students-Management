package com.example.stduents_management.student.repository;

import com.example.stduents_management.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    boolean existsByStudentCode(String studentCode);

    boolean existsByStudentCodeAndStudentIdNot(
            String studentCode,
            UUID studentId
    );

    Page<Student> findByStudentCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );
}

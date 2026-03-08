package com.example.stduents_management.student.repository;

import com.example.stduents_management.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    /** Số sinh viên theo từng khoa (để vẽ biểu đồ). */
    @Query("SELECT f.facultyName, COUNT(s) FROM Student s JOIN s.clazz c JOIN c.major m JOIN m.faculty f GROUP BY f.facultyId, f.facultyName ORDER BY COUNT(s) DESC")
    List<Object[]> countStudentsByFaculty();

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

    Optional<Student> findByStudentCodeIgnoreCase(String studentCode);
}

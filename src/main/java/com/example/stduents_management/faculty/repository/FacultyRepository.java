package com.example.stduents_management.faculty.repository;

import com.example.stduents_management.faculty.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {

    boolean existsByFacultyCode(String facultyCode);

    boolean existsByFacultyCodeAndFacultyIdNot(
            String facultyCode,
            UUID facultyId
    );

    // tìm kiếm gần đúng theo mã khoa hoặc tên khoa
    Page<Faculty> findByFacultyCodeContainingIgnoreCaseOrFacultyNameContainingIgnoreCase(
            String facultyCode,
            String facultyName,
            Pageable pageable
    );
    List<Faculty> findAll(Sort sort);
}

package com.example.stduents_management.lecturer.repository;

import com.example.stduents_management.employee.entity.EmployeeType;
import com.example.stduents_management.lecturer.entity.Lecturer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LecturerRepository extends JpaRepository<Lecturer, UUID> {

    @Query("SELECT l FROM Lecturer l JOIN FETCH l.faculty ORDER BY l.lecturerCode")
    List<Lecturer> findAllWithFacultyOrderByLecturerCode();

    @Query("SELECT l FROM Lecturer l LEFT JOIN FETCH l.faculty WHERE l.employee.employeeId = :eid")
    Optional<Lecturer> findByEmployee_EmployeeId(@Param("eid") UUID employeeId);

    boolean existsByEmployee_EmployeeId(UUID employeeId);

    @Query("""
            SELECT l FROM Lecturer l
            WHERE (l.employee IS NULL OR l.employee.employeeType = :lecturerOnly)
              AND (:kw IS NULL OR :kw = ''
                   OR LOWER(l.lecturerCode) LIKE LOWER(CONCAT('%', :kw, '%'))
                   OR LOWER(l.fullName) LIKE LOWER(CONCAT('%', :kw, '%')))
            """)
    Page<Lecturer> pageTeachingLecturers(
            @Param("kw") String kw,
            @Param("lecturerOnly") EmployeeType lecturerOnly,
            Pageable pageable);

    @Query("""
            SELECT l FROM Lecturer l
            WHERE l.employee IS NULL OR l.employee.employeeType = :lecturerOnly
            ORDER BY l.lecturerCode
            """)
    List<Lecturer> findTeachingLecturersOrderByCode(@Param("lecturerOnly") EmployeeType lecturerOnly);

    Optional<Lecturer> findByLecturerCodeIgnoreCase(String lecturerCode);

    boolean existsByLecturerCodeIgnoreCase(String lecturerCode);

    boolean existsByLecturerCode(String lecturerCode);

    boolean existsByLecturerCodeAndLecturerIdNot(String lecturerCode, UUID id);

    Page<Lecturer> findByLecturerCodeContainingIgnoreCaseOrFullNameContainingIgnoreCase(
            String code,
            String name,
            Pageable pageable
    );

    boolean existsByPosition_PositionId(UUID positionId);

    boolean existsByLecturerDuty_LecturerDutyId(UUID lecturerDutyId);

    boolean existsByDepartment_DepartmentId(UUID departmentId);
}

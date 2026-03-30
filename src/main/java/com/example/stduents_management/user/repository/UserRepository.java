package com.example.stduents_management.user.repository;

import com.example.stduents_management.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByStudent_StudentId(UUID studentId);
    boolean existsByStudent_StudentIdAndIdNot(UUID studentId, UUID id);
    boolean existsByLecturer_LecturerId(UUID lecturerId);
    boolean existsByLecturer_LecturerIdAndIdNot(UUID lecturerId, UUID id);

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);

    /** Load user với roles, student, lecturer để hiển thị profile. */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.roles " +
            "LEFT JOIN FETCH u.student " +
            "LEFT JOIN FETCH u.lecturer " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithProfile(@Param("username") String username);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username,
            String email,
            Pageable pageable
    );
}

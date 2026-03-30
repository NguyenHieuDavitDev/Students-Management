package com.example.stduents_management.user.service;

import com.example.stduents_management.lecturer.repository.LecturerRepository;
import com.example.stduents_management.role.entity.Role;
import com.example.stduents_management.role.repository.RoleRepository;
import com.example.stduents_management.student.repository.StudentRepository;
import com.example.stduents_management.user.dto.RegisterRequest;
import com.example.stduents_management.user.dto.UserRequest;
import com.example.stduents_management.user.dto.UserResponse;
import com.example.stduents_management.user.entity.User;
import com.example.stduents_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final PasswordEncoder passwordEncoder;


    public Page<UserResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username"));

        Page<User> users = (keyword == null || keyword.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, pageable);

        return users.map(this::toResponse);
    }

    public UserResponse getById(UUID id) {
        return toResponse(findUser(id));
    }


    @Transactional
    public UserResponse create(UserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        validateStudentAndLecturerAssignment(null, request);

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(request.isEnabled());
        user.setRoles(loadRoles(request.getRoleIds()));
        setStudentAndLecturer(user, request);

        userRepository.save(user);
        return toResponse(user);
    }


    @Transactional
    public UserResponse update(UUID id, UserRequest request) {
        User user = findUser(id);

        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        validateStudentAndLecturerAssignment(id, request);

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setEnabled(request.isEnabled());

        // chỉ đổi password khi có nhập
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setRoles(loadRoles(request.getRoleIds()));
        setStudentAndLecturer(user, request);
        return toResponse(user);
    }

    /**
     * Đăng ký tài khoản sinh viên (chỉ gán role STUDENT).
     */
    @Transactional
    public void registerStudent(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Xác nhận mật khẩu không khớp");
        }
        Role studentRole = roleRepository.findByNameIgnoreCase("STUDENT")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Vai trò STUDENT chưa được cấu hình"));
        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(Set.of(studentRole));
        userRepository.save(user);
    }


    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }


    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Set<Role> loadRoles(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return Set.of();
        return roleIds.stream()
                .map(id -> roleRepository.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found")))
                .collect(Collectors.toSet());
    }

    private void setStudentAndLecturer(User user, UserRequest request) {
        user.setStudent(request.getStudentId() != null
                ? studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"))
                : null);
        if (request.getLecturerId() == null) {
            user.setLecturer(null);
            return;
        }

        var lecturer = lecturerRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecturer not found"));
        user.setLecturer(lecturer);
    }

    private void validateStudentAndLecturerAssignment(UUID userId, UserRequest request) {
        if (request.getStudentId() != null && request.getLecturerId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A user can only be assigned to either a student or a lecturer"
            );
        }

        if (request.getStudentId() != null) {
            boolean assigned = userId == null
                    ? userRepository.existsByStudent_StudentId(request.getStudentId())
                    : userRepository.existsByStudent_StudentIdAndIdNot(request.getStudentId(), userId);
            if (assigned) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Student already has an account");
            }
        }

        if (request.getLecturerId() != null) {
            boolean assigned = userId == null
                    ? userRepository.existsByLecturer_LecturerId(request.getLecturerId())
                    : userRepository.existsByLecturer_LecturerIdAndIdNot(request.getLecturerId(), userId);
            if (assigned) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Lecturer already has an account");
            }
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()),
                user.getStudent() != null ? user.getStudent().getStudentId() : null,
                user.getLecturer() != null ? user.getLecturer().getLecturerId() : null
        );
    }
}

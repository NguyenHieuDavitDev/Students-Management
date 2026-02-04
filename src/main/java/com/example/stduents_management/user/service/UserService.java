package com.example.stduents_management.user.service;

import com.example.stduents_management.role.entity.Role;
import com.example.stduents_management.role.repository.RoleRepository;
import com.example.stduents_management.user.dto.UserRequest;
import com.example.stduents_management.user.dto.UserResponse;
import com.example.stduents_management.user.entity.User;
import com.example.stduents_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
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

    /* ================= SEARCH ================= */

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

    /* ================= CREATE ================= */

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

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(request.getPassword()); // 👉 sau này encode
        user.setEnabled(request.isEnabled());
        user.setRoles(loadRoles(request.getRoleIds()));

        userRepository.save(user);
        return toResponse(user);
    }

    /* ================= UPDATE ================= */

    @Transactional
    public UserResponse update(UUID id, UserRequest request) {
        User user = findUser(id);

        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setEnabled(request.isEnabled());

        // chỉ đổi password khi có nhập
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());
        }

        user.setRoles(loadRoles(request.getRoleIds()));
        return toResponse(user);
    }

    /* ================= DELETE ================= */

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    /* ================= PRIVATE ================= */

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

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }
}

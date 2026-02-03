package com.example.stduents_management.role.service;


import com.example.stduents_management.role.dto.RoleRequest;
import com.example.stduents_management.role.dto.RoleResponse;
import com.example.stduents_management.role.entity.Role;
import com.example.stduents_management.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream()
                .map(r -> new RoleResponse(
                        r.getId(),
                        r.getName(),
                        r.getDescription()
                ))
                .toList();
    }

    private static String normalizeName(String name) {
        return name == null ? null : name.trim();
    }
    // tìm kiếm gần đúng + phân trang
    public Page<RoleResponse> search(
            String keyword,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("name").ascending()
        );

        Page<Role> roles;

        if (keyword == null || keyword.trim().isEmpty()) {
            roles = roleRepository.findAll(pageable);
        } else {
            roles = roleRepository
                    .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                            keyword,
                            keyword,
                            pageable
                    );
        }

        return roles.map(r ->
                new RoleResponse(
                        r.getId(),
                        r.getName(),
                        r.getDescription()
                )
        );
    }


    public RoleResponse getById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        String name = normalizeName(request.getName());
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (roleRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name already exists");
        }

        Role role = new Role();
        role.setName(name);
        role.setDescription(request.getDescription());
        roleRepository.save(role);
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }

    @Transactional
    public RoleResponse update(UUID id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        String name = normalizeName(request.getName());
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        if (roleRepository.existsByNameAndIdNot(name, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role name already exists");
        }

        role.setName(name);
        role.setDescription(request.getDescription());
        roleRepository.save(role);
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }

    @Transactional
    public void delete(UUID id) {
        if (!roleRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }
        roleRepository.deleteById(id);
    }
}

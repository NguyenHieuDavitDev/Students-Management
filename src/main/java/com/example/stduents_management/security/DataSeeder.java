package com.example.stduents_management.security;

import com.example.stduents_management.role.entity.Role;
import com.example.stduents_management.role.repository.RoleRepository;
import com.example.stduents_management.user.entity.User;
import com.example.stduents_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_USERNAME = "admin1";
    private static final String DEFAULT_ADMIN_PASSWORD = "123456";
    private static final String DEFAULT_ADMIN_EMAIL = "admin1@example.com";
    private static final String ROLE_ADMIN = "ADMIN";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findByUsername(DEFAULT_ADMIN_USERNAME).isPresent()) {
            return;
        }
        Role adminRole = roleRepository.findByNameIgnoreCase(ROLE_ADMIN)
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName(ROLE_ADMIN);
                    r.setDescription("Quản trị viên - được truy cập dashboard");
                    return roleRepository.save(r);
                });
        User admin = new User();
        admin.setUsername(DEFAULT_ADMIN_USERNAME);
        admin.setEmail(DEFAULT_ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
        admin.setEnabled(true);
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
        log.info("Đã tạo tài khoản mặc định: {} / {}", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
    }
}

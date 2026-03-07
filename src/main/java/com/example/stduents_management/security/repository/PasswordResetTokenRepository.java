package com.example.stduents_management.security.repository;

import com.example.stduents_management.security.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenAndExpiryAtAfter(String token, Instant now);

    void deleteByUser_Id(UUID userId);

    void deleteByExpiryAtBefore(Instant now);
}

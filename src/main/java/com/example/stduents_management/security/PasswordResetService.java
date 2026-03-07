package com.example.stduents_management.security;

import com.example.stduents_management.security.entity.PasswordResetToken;
import com.example.stduents_management.security.repository.PasswordResetTokenRepository;
import com.example.stduents_management.user.entity.User;
import com.example.stduents_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiry-hours:24}")
    private int expiryHours = 24;

    /**
     * Tạo token đặt lại mật khẩu cho user có email tương ứng. Trả về reset link (để gửi email hoặc hiển thị khi dev).
     */
    @Transactional
    public Optional<String> createTokenForEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        User user = userRepository.findByEmail(email.trim()).orElse(null);
        if (user == null) return Optional.empty();

        tokenRepository.deleteByUser_Id(user.getId());
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiry = Instant.now().plusSeconds(expiryHours * 3600L);
        PasswordResetToken prt = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryAt(expiry)
                .build();
        tokenRepository.save(prt);
        return Optional.of(token);
    }

    /**
     * Xác thực token và đổi mật khẩu. Ném exception nếu token không hợp lệ hoặc hết hạn.
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token không hợp lệ");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới tối thiểu 6 ký tự");
        }
        PasswordResetToken prt = tokenRepository.findByTokenAndExpiryAtAfter(token, Instant.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn"));
        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(prt);
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) return false;
        return tokenRepository.findByTokenAndExpiryAtAfter(token, Instant.now()).isPresent();
    }
}

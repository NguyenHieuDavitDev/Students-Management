package com.example.stduents_management.web;

import com.example.stduents_management.user.dto.UserProfileDto;
import com.example.stduents_management.user.service.CurrentUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thêm attribute "userProfile" vào mọi view (header, sidebar hiển thị profile).
 * Khi chưa đăng nhập, userProfile = null.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class UserProfileModelAdvice {

    private final CurrentUserProfileService currentUserProfileService;

    @ModelAttribute("userProfile")
    public UserProfileDto userProfile() {
        return currentUserProfileService.getCurrentProfile().orElse(null);
    }
}

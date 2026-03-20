package com.example.stduents_management.notification.controller;

import com.example.stduents_management.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class NotificationHeaderModelAdvice {

    private final NotificationService notificationService;

    @ModelAttribute("unreadNotificationCount")
    public Long unreadNotificationCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return 0L;
        }
        try {
            return notificationService.countUnreadForCurrentUser();
        } catch (Exception ignored) {
            return 0L;
        }
    }
}


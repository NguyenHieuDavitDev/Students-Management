package com.example.stduents_management.notification.controller;

import com.example.stduents_management.notification.entity.NotificationCategory;
import com.example.stduents_management.notification.dto.NotificationResponse;
import com.example.stduents_management.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class NotificationDashboardController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public String index(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        NotificationCategory parsedCategory = parseCategory(category).orElse(null);
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponse> result = notificationService.searchForCurrentUser(parsedCategory, unreadOnly, pageable);
        model.addAttribute("items", result.getContent());
        model.addAttribute("page", result);
        model.addAttribute("category", parsedCategory);
        model.addAttribute("categoryStr", category);
        model.addAttribute("unreadOnly", unreadOnly);
        model.addAttribute("categories", NotificationCategory.values());
        model.addAttribute("size", size);
        model.addAttribute("pageIndex", page);

        return "notifications/index";
    }

    @PostMapping("/notifications/{id}/read")
    public String markAsRead(
            @PathVariable UUID id,
            RedirectAttributes redirect
    ) {
        notificationService.markAsRead(id);
        redirect.addFlashAttribute("success", "Đã đánh dấu là đã đọc");
        return "redirect:/notifications";
    }

    private Optional<NotificationCategory> parseCategory(String categoryStr) {
        if (categoryStr == null || categoryStr.isBlank()) return Optional.empty();
        try {
            return Optional.of(NotificationCategory.valueOf(categoryStr.trim().toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}


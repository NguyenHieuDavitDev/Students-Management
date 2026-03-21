package com.example.stduents_management.notification.controller;

import com.example.stduents_management.notification.dto.NotificationCreateRequest;
import com.example.stduents_management.notification.dto.NotificationResponse;
import com.example.stduents_management.notification.entity.NotificationCategory;
import com.example.stduents_management.notification.service.NotificationService;
import com.example.stduents_management.user.entity.User;
import com.example.stduents_management.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminDashboardController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public String index(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID recipientUserId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        NotificationCategory parsedCategory = parseCategory(category).orElse(null);
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationResponse> result = notificationService.searchForAdmin(
                parsedCategory,
                recipientUserId,
                unreadOnly,
                keyword,
                pageable
        );

        model.addAttribute("page", result);
        model.addAttribute("items", result.getContent());
        model.addAttribute("categories", NotificationCategory.values());
        model.addAttribute("category", parsedCategory);
        model.addAttribute("categoryStr", category == null ? "" : category);
        model.addAttribute("recipientUserId", recipientUserId);
        model.addAttribute("unreadOnly", unreadOnly);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("size", size);

        return "notifications/admin/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setScheduledAt(LocalDateTime.now());

        model.addAttribute("mode", "create");
        model.addAttribute("notificationRequest", req);
        model.addAttribute("notificationId", null);
        model.addAttribute("users", userRepository.findAll(Sort.by("username")));
        model.addAttribute("categories", NotificationCategory.values());

        return "notifications/admin/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("notificationRequest") NotificationCreateRequest req,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "create");
            model.addAttribute("notificationId", null);
            model.addAttribute("users", userRepository.findAll(Sort.by("username")));
            model.addAttribute("categories", NotificationCategory.values());
            return "notifications/admin/form";
        }

        notificationService.createManual(req);
        return "redirect:/admin/notifications";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        NotificationResponse item = notificationService.getById(id);

        NotificationCreateRequest req = new NotificationCreateRequest();
        req.setRecipientUserId(item.recipientUserId());
        req.setCategory(item.category());
        req.setTitle(item.title());
        req.setContent(item.content());
        req.setScheduledAt(item.scheduledAt());

        model.addAttribute("mode", "edit");
        model.addAttribute("notificationId", id);
        model.addAttribute("notificationRequest", req);
        model.addAttribute("users", userRepository.findAll(Sort.by("username")));
        model.addAttribute("categories", NotificationCategory.values());

        return "notifications/admin/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable UUID id,
            @Valid @ModelAttribute("notificationRequest") NotificationCreateRequest req,
            BindingResult bindingResult,
            RedirectAttributes redirect,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("notificationId", id);
            model.addAttribute("users", userRepository.findAll(Sort.by("username")));
            model.addAttribute("categories", NotificationCategory.values());
            return "notifications/admin/form";
        }

        notificationService.updateManual(id, req);
        redirect.addFlashAttribute("success", "Cập nhật thông báo thành công");
        return "redirect:/admin/notifications";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id, RedirectAttributes redirect) {
        notificationService.deleteManual(id);
        redirect.addFlashAttribute("success", "Xóa thông báo thành công");
        return "redirect:/admin/notifications";
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


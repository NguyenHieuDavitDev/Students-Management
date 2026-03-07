package com.example.stduents_management.security;

import com.example.stduents_management.user.dto.RegisterRequest;
import com.example.stduents_management.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    @GetMapping
    public String form(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping
    public String submit(
            @Valid RegisterRequest registerRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("registerRequest", registerRequest);
            return "register";
        }
        try {
            userService.registerStudent(registerRequest);
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công. Bạn có thể đăng nhập.");
            return "redirect:/login";
        } catch (org.springframework.web.server.ResponseStatusException e) {
            model.addAttribute("registerRequest", registerRequest);
            model.addAttribute("globalError", e.getReason());
            return "register";
        }
    }
}

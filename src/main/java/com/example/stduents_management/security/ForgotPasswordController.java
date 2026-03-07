package com.example.stduents_management.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    @Value("${server.port:8080}")
    private int serverPort;

    @GetMapping("/forgot-password")
    public String form(
            @RequestParam(required = false) Boolean sent,
            @RequestParam(required = false) String resetLink,
            Model model
    ) {
        model.addAttribute("sent", Boolean.TRUE.equals(sent));
        model.addAttribute("resetLink", resetLink); // từ flash hoặc query (dev)
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String submit(
            @RequestParam String email,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        Optional<String> tokenOpt = passwordResetService.createTokenForEmail(email);
        if (tokenOpt.isPresent()) {
            String baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), "");
            String link = baseUrl + "/reset-password?token=" + tokenOpt.get();
            redirectAttributes.addFlashAttribute("resetLink", link);
            redirectAttributes.addFlashAttribute("message", "Nếu email tồn tại trong hệ thống, đường dẫn đặt lại mật khẩu đã được gửi. (Để kiểm tra, link cũng hiển thị bên dưới.)");
        } else {
            redirectAttributes.addFlashAttribute("message", "Nếu email tồn tại trong hệ thống, bạn sẽ nhận được đường dẫn đặt lại mật khẩu.");
        }
        redirectAttributes.addAttribute("sent", true);
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetForm(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isBlank() || !passwordResetService.isTokenValid(token)) {
            model.addAttribute("invalidToken", true);
            return "reset-password";
        }
        model.addAttribute("token", token);
        model.addAttribute("invalidToken", false);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Xác nhận mật khẩu không khớp.");
            return "reset-password";
        }
        try {
            passwordResetService.resetPassword(token, newPassword);
            redirectAttributes.addFlashAttribute("message", "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage() != null ? e.getMessage() : "Link không hợp lệ hoặc đã hết hạn.");
            return "reset-password";
        }
    }
}

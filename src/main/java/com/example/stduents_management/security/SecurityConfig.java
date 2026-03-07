package com.example.stduents_management.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error", "/css/**", "/js/**", "/webjars/**").permitAll()
                        .requestMatchers("/register", "/register/**").permitAll()
                        .requestMatchers("/forgot-password", "/forgot-password/**").permitAll()
                        .requestMatchers("/reset-password", "/reset-password/**").permitAll()
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/roles", "/admin/roles/**").hasRole("ADMIN")
                        .requestMatchers("/admin/users", "/admin/users/**").hasRole("ADMIN")
                        .requestMatchers("/admin/permissions", "/admin/permissions/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER", "LECTURER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler())
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .userDetailsService(userDetailsService)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedPage("/?denied")
                );
        return http.build();
    }

    private static final Set<String> DASHBOARD_ROLES = Set.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_LECTURER");
    private static final String ROLE_STUDENT = "ROLE_STUDENT";

    /** Khi chưa đăng nhập: truy cập /admin/** → chuyển tới /admin/login; còn lại → chuyển tới trang chủ /. */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            String ctx = request.getContextPath();
            String path = request.getRequestURI();
            boolean isAdminArea = path != null && (path.equals(ctx + "/admin") || path.startsWith(ctx + "/admin/"));
            boolean isAdminLogin = path != null && path.equals(ctx + "/admin/login");
            if (isAdminArea && !isAdminLogin) {
                response.sendRedirect(ctx + "/admin/login");
            } else {
                response.sendRedirect(ctx + "/");
            }
        };
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String referer = request.getHeader("Referer");
            String ctx = request.getContextPath();
            boolean fromAdminLogin = referer != null && referer.contains("/admin/login");
            String target = fromAdminLogin ? ctx + "/admin/login?error" : ctx + "/?error";
            response.sendRedirect(target);
        };
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) -> {
            Set<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            boolean canAccessDashboard = authorities.stream().anyMatch(DASHBOARD_ROLES::contains);
            boolean isStudent = authorities.contains(ROLE_STUDENT);
            if (canAccessDashboard) {
                response.sendRedirect(request.getContextPath() + "/admin");
            } else if (isStudent) {
                response.sendRedirect(request.getContextPath() + "/");
            } else {
                response.sendRedirect(request.getContextPath() + "/login?denied");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

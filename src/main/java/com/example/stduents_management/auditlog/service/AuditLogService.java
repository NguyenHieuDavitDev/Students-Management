package com.example.stduents_management.auditlog.service;

import com.example.stduents_management.auditlog.dto.AuditLogResponse;
import com.example.stduents_management.auditlog.entity.AuditLog;
import com.example.stduents_management.auditlog.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(
            String action,
            String moduleName,
            String targetId,
            String description,
            String httpMethod,
            String requestPath,
            String ipAddress,
            String userAgent,
            Integer statusCode
    ) {
        AuditLog a = new AuditLog();
        a.setAction(nvl(action, "UNKNOWN"));
        a.setModuleName(nvl(moduleName, "system"));
        a.setTargetId(trimToNull(targetId));
        a.setDescription(trimToNull(description));
        a.setUsername(currentUsername());
        a.setHttpMethod(trimToNull(httpMethod));
        a.setRequestPath(trimToNull(requestPath));
        a.setIpAddress(trimToNull(ipAddress));
        a.setUserAgent(trimToNull(userAgent));
        a.setStatusCode(statusCode);
        auditLogRepository.save(a);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(
            String keyword,
            String action,
            String moduleName,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        LocalDateTime fromAt = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toAt = toDate != null ? toDate.atTime(LocalTime.MAX) : null;
        return auditLogRepository.search(keyword, action, moduleName, fromAt, toAt, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> latest(int limit) {
        if (limit <= 0 || limit > 300) {
            limit = 100;
        }
        List<AuditLog> rows = auditLogRepository.findTop100ByOrderByCreatedAtDesc();
        return rows.stream().limit(limit).map(this::toResponse).toList();
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getAction(),
                a.getModuleName(),
                a.getTargetId(),
                a.getDescription(),
                a.getUsername(),
                a.getHttpMethod(),
                a.getRequestPath(),
                a.getIpAddress(),
                a.getUserAgent(),
                a.getStatusCode(),
                a.getCreatedAt()
        );
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "anonymous";
        }
        return nvl(auth.getName(), "unknown");
    }

    private String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String nvl(String value, String fallback) {
        String t = trimToNull(value);
        return t != null ? t : fallback;
    }
}

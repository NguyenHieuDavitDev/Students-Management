package com.example.stduents_management.auditlog.web;

import com.example.stduents_management.auditlog.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuditLogInterceptor implements HandlerInterceptor {

    private final AuditLogService auditLogService;

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        String method = request.getMethod();
        if (!isWriteMethod(method)) {
            return;
        }

        String path = request.getRequestURI();
        if (path == null) {
            return;
        }
        if (!(path.startsWith("/admin/") || path.startsWith("/api/"))) {
            return;
        }
        // Tránh ghi đệ quy cho module audit.
        if (path.startsWith("/admin/audit-logs") || path.startsWith("/api/audit-logs")) {
            return;
        }

        String action = mapAction(method);
        String moduleName = moduleFromPath(path);
        String targetId = targetIdFromPath(path);
        String ip = readClientIp(request);
        String ua = request.getHeader("User-Agent");
        String desc = buildDescription(action, moduleName, targetId, path, response.getStatus(), ex);

        auditLogService.log(
                action,
                moduleName,
                targetId,
                desc,
                method,
                path,
                ip,
                ua,
                response.getStatus()
        );
    }

    private boolean isWriteMethod(String method) {
        if (method == null) {
            return false;
        }
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method)
                || "DELETE".equalsIgnoreCase(method);
    }

    private String mapAction(String method) {
        if ("POST".equalsIgnoreCase(method)) {
            return "CREATE";
        }
        if ("PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method)) {
            return "UPDATE";
        }
        if ("DELETE".equalsIgnoreCase(method)) {
            return "DELETE";
        }
        return "UNKNOWN";
    }

    private String moduleFromPath(String path) {
        String[] parts = path.split("/");
        // /admin/{module}/... hoặc /api/{module}/...
        return parts.length >= 3 ? parts[2] : "unknown";
    }

    private String targetIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length >= 4) {
            String last = parts[parts.length - 1];
            if (!"delete".equalsIgnoreCase(last) && !"edit".equalsIgnoreCase(last) && !"new".equalsIgnoreCase(last)) {
                return last;
            }
            if (parts.length >= 5) {
                return parts[parts.length - 2];
            }
        }
        return null;
    }

    private String readClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String[] ips = xff.split(",");
            if (ips.length > 0) {
                return ips[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String buildDescription(
            String action,
            String moduleName,
            String targetId,
            String path,
            int statusCode,
            Exception ex
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(action).append(" ").append(moduleName);
        if (targetId != null && !targetId.isBlank()) {
            sb.append(" #").append(targetId);
        }
        sb.append(" (").append(statusCode).append(")");
        if (ex != null) {
            sb.append(" error=").append(ex.getClass().getSimpleName());
        }
        sb.append(" path=").append(path);
        return sb.toString();
    }
}

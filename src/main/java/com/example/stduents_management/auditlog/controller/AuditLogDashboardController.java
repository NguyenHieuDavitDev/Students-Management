package com.example.stduents_management.auditlog.controller;

import com.example.stduents_management.auditlog.dto.AuditLogResponse;
import com.example.stduents_management.auditlog.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogDashboardController {

    private final AuditLogService auditLogService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String moduleName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Page<AuditLogResponse> logs = auditLogService.search(
                keyword, action, moduleName, fromDate, toDate, page, size
        );
        model.addAttribute("logs", logs);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("action", action == null ? "" : action);
        model.addAttribute("moduleName", moduleName == null ? "" : moduleName);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        return "audit-logs/index";
    }

    @GetMapping("/api/latest")
    @ResponseBody
    public ResponseEntity<List<AuditLogResponse>> latest(
            @RequestParam(defaultValue = "100") int limit
    ) {
        return ResponseEntity.ok(auditLogService.latest(limit));
    }
}

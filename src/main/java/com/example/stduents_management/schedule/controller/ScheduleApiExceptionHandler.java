package com.example.stduents_management.schedule.controller;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = ScheduleApiController.class)
public class ScheduleApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        String msg = ex.getReason() != null ? ex.getReason() : "Lỗi";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", detail.isEmpty() ? "Dữ liệu không hợp lệ" : detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private String formatFieldError(FieldError e) {
        String field = e.getField();
        String msg = e.getDefaultMessage() != null ? e.getDefaultMessage() : "không hợp lệ";
        return field + ": " + msg;
    }

    /**
     * JSON sai cú pháp, enum không khớp (vd. sessionType), hoặc kiểu trường sai — trả message rõ cho lịch (kể cả tăng cường).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException ex) {
        Throwable root = ex.getMostSpecificCause();
        String hint = root != null ? root.getMessage() : ex.getMessage();
        String msg = "Dữ liệu gửi lên không đọc được (JSON sai hoặc giá trị không hợp lệ).";
        if (hint != null) {
            String h = hint.toLowerCase();
            if (h.contains("sessiontype") || h.contains("session_type")) {
                msg = "Loại buổi (sessionType) không hợp lệ — dùng đúng tên: THEORY, PRACTICE, EXAM, SUPPLEMENTARY.";
            } else if (h.contains("uuid") || h.contains("lecturer")) {
                msg = "Mã giảng viên (lecturerId) phải là UUID hợp lệ.";
            } else if (root instanceof JsonMappingException jme && jme.getPath() != null && !jme.getPath().isEmpty()) {
                String field = jme.getPath().getFirst().getFieldName();
                if (field != null) {
                    msg = "Trường «" + field + "» không hợp lệ hoặc sai kiểu.";
                }
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", msg));
    }

    /**
     * CHECK trên DB (vd. session_type chưa có SUPPLEMENTARY) — tránh stack trace trần cho client.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        Throwable root = ex.getMostSpecificCause();
        String raw = root != null ? root.getMessage() : ex.getMessage();
        String msg = "Dữ liệu không lưu được do ràng buộc cơ sở dữ liệu.";
        if (raw != null) {
            String u = raw.toUpperCase();
            if (u.contains("SESSION_TYPE") || (u.contains("CHECK") && u.contains("SCHEDULES"))) {
                msg = "Ràng buộc CHECK trên bảng schedules (thường là session_type) chưa cho phép SUPPLEMENTARY hoặc còn CHECK cũ song song. "
                        + "Khởi động lại ứng dụng để Flyway chạy V20260406140000, hoặc chạy tay file database/migration_schedules_session_type_supplementary.sql trên SQL Server, rồi thử lại.";
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", msg));
    }
}

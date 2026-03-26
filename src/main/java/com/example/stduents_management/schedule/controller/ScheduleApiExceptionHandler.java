package com.example.stduents_management.schedule.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}

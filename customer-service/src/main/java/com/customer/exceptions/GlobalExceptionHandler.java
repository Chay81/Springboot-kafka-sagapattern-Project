package com.customer.exceptions;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Handle custom customer not found exception
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex, HttpServletRequest request) {
        log.warn("❌ Customer not found: {}", ex.getMessage());

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("status", HttpStatus.NO_CONTENT.value());
        errorBody.put("error", "No Content");
        errorBody.put("message", ex.getMessage());
        errorBody.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorBody);
    }

    // 2. Handle validation errors from @Valid DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("⚠️ Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                errors.toString(),
                request.getRequestURI()
        );
    }

    // 3. Handle invalid/malformed JSON body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("⚠️ Malformed JSON: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON",
                "Request body is invalid or unreadable.",
                request.getRequestURI()
        );
    }

    // 4. Handle method not allowed (e.g., POST on GET endpoint)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMethod(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("⚠️ Unsupported HTTP Method: {}", ex.getMethod());

        return buildErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method Not Allowed",
                "HTTP method not supported: " + ex.getMethod(),
                request.getRequestURI()
        );
    }

    // 5. Handle Spring security access denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("⛔ Access Denied: {}", ex.getMessage());

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("status", HttpStatus.FORBIDDEN.value());
        errorBody.put("error", "Forbidden");
        errorBody.put("message", "You do not have permission to access this resource.");
        errorBody.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
    }

    // 6. Handle all other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("❌ Unexpected error: {}", ex.getMessage(), ex);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", ex.getMessage());
        errorBody.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }

    // Reusable response builder
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, String message, String path) {

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", Instant.now().toString());
        errorBody.put("status", status.value());
        errorBody.put("error", error);
        errorBody.put("message", message);
        errorBody.put("path", path);

        return ResponseEntity.status(status).body(errorBody);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolations(
            ConstraintViolationException ex, HttpServletRequest request) {

        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                errors.toString(),
                request.getRequestURI()
        );
    }

}

package com.ginning.erp.common.exception;

import com.ginning.erp.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApplicationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<?>> handleApplicationException(
            ApplicationException ex, WebRequest request) {
        logger.error("Application exception: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(ex.getHttpStatusCode())
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), ex.getHttpStatusCode()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(404)
                .body(ApiResponse.error(ex.getErrorCode(), ex.getMessage(), 404));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        logger.warn("Validation error: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", 400));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "You do not have permission to access this resource", 403));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(
            Exception ex, WebRequest request) {
        logger.error("Unexpected exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred", 500));
    }

}

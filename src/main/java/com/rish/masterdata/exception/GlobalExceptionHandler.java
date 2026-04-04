package com.rish.masterdata.exception;

import com.rish.masterdata.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─── DUPLICATE EMAIL ─────────────────────────────

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException ex) {

        log.warn("Duplicate email: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)        // 409
                .body(ErrorResponse.builder()
                        .status(409)
                        .error("Conflict")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── USER NOT FOUND ──────────────────────────────

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex) {

        log.warn("User not found: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)       // 404
                .body(ErrorResponse.builder()
                        .status(404)
                        .error("Not Found")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── INVALID CREDENTIALS ─────────────────────────

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex) {

        log.warn("Invalid credentials attempt");

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)    // 401
                .body(ErrorResponse.builder()
                        .status(401)
                        .error("Unauthorized")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── ACCOUNT STATUS ──────────────────────────────

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ErrorResponse> handleAccountStatus(
            AccountStatusException ex) {

        log.warn("Account status issue: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)       // 403
                .body(ErrorResponse.builder()
                        .status(403)
                        .error("Forbidden")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── VALIDATION ERRORS (@Valid) ──────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)     // 400
                .body(ErrorResponse.builder()
                        .status(400)
                        .error("Bad Request")
                        .message(errors)
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // ─── CATCH ALL ───────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)   // 500
                .body(ErrorResponse.builder()
                        .status(500)
                        .error("Internal Server Error")
                        .message("An unexpected error occurred")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
package com.rish.masterdata.controller;

import com.rish.masterdata.dto.AuthResponse;
import com.rish.masterdata.dto.ErrorResponse;
import com.rish.masterdata.dto.LoginRequest;
import com.rish.masterdata.dto.RegistrationRequest;
import com.rish.masterdata.service.JwtService;
import com.rish.masterdata.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    public final UserService userService;
    public final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registration(@Valid @RequestBody RegistrationRequest request) {

        log.info("Registration request for email: {}",
                request.getEmail());
        AuthResponse response = userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request for email: {}",
                request.getEmail());
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    // OAuth2 success redirect endpoint
    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponse> oauth2Success(
            @RequestParam String token) {

        // Extract claims from token
        String userId = jwtService.extractUserId(token);
        String email  = jwtService.extractEmail(token);
        String role   = jwtService.extractRole(token);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .userId(userId)
                .email(email)
                .role(role)
                .build());
    }

    @GetMapping("/oauth2/error")
    public ResponseEntity<ErrorResponse> oauth2Error() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .status(401)
                        .error("OAuth2 Error")
                        .message("OAuth2 authentication failed")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

}

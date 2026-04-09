package com.rish.masterdata.controller;


import com.rish.masterdata.dto.UserResponse;
import com.rish.masterdata.service.JwtService;
import com.rish.masterdata.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final UserService userService;
    private final JwtService jwtService;

    // Get Current User
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @RequestHeader(value = "Authorization",
                    required = false) String authHeader,
            @AuthenticationPrincipal String userId) {

        // userId injected from SecurityContext
        // SecurityContext populated by JwtAuthFilter
        // JwtAuthFilter reads from cookie OR header
        // So userId should always be available here

        log.info("Fetching user: {}", userId);

        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String userId) {

        UserResponse response = userService
                .getUserById(userId);
        return ResponseEntity.ok(response);
    }


}

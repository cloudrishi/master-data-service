package com.rish.masterdata.controller;


import com.rish.masterdata.dto.UserResponse;
import com.rish.masterdata.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/master-data")
@RequiredArgsConstructor
public class MasterDataController {

    private final UserService userService;

    // Get Current User
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal String userId) {

        log.info("Fetching user: {}", userId);

        UserResponse response = userService
                .getUserById(userId);

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

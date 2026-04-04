package com.rish.masterdata.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
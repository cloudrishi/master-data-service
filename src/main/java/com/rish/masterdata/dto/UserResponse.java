package com.rish.masterdata.dto;

import com.rish.masterdata.entity.AccountStatus;
import com.rish.masterdata.entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String timezone;
    private String preferredLanguage;
    private AccountStatus accountStatus;
    private Role role;
    private Set<AddressResponse> addresses;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
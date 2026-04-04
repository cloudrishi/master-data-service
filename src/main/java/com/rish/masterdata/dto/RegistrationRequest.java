package com.rish.masterdata.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20,
            message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain uppercase, lowercase, number and special character"
    )
    private String password;

    private String timezone;

    private String preferredLanguage;

    private Set<AddressRequest> addresses;
}
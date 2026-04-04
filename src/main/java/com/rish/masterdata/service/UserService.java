package com.rish.masterdata.service;


import com.rish.masterdata.dto.*;
import com.rish.masterdata.entity.*;
import com.rish.masterdata.exception.AccountStatusException;
import com.rish.masterdata.exception.DuplicateEmailException;
import com.rish.masterdata.exception.InvalidCredentialsException;
import com.rish.masterdata.exception.UserNotFoundException;
import com.rish.masterdata.repository.AddressRepository;
import com.rish.masterdata.repository.UserCredentialsRepository;
import com.rish.masterdata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserCredentialsRepository credentialsRepository;
    private final AddressRepository addressRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegistrationRequest request) {

        // 1. Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already registered");
        }

        // 2. Build and save User Entity
        User user = User.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .timeZone(request.getTimezone())
                .preferredLanguage(request.getPreferredLanguage())
                .build();

        User savedUser = userRepository.save(user);

        // 3. Save addresses if provided
        if (request.getAddresses() != null) {
            Set<Address> addresses = request.getAddresses()
                    .stream()
                    .map(a -> Address.builder()
                                    .addressType(a.getAddressType())
                                    .street(a.getStreet())
                                    .street2(a.getStreet2())
                                    .city(a.getCity())
                                    .state(a.getState())
                                    .zip(a.getZip())
                                    .country(a.getCountry())
                                    .isDefault(a.getIsDefault())
                                    .user(savedUser)
                                    .build())
                    .collect(Collectors.toSet());
            addressRepository.saveAll(addresses);
        }

        // 4. Hash password and save credentials
        UserCredentials credentials = UserCredentials.builder()
                .user(savedUser)
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .build();

        credentialsRepository.save(credentials);

        // 5. Generate JWT and return
        String token = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole().name())
                .build();

    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        // 1. Find user by email
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password"));

        // 2. Check account status
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountStatusException(
                    "Account is " + user.getAccountStatus().name().toLowerCase()
            );
        }

        // 3. Find credentials
        UserCredentials credentials = credentialsRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid email or password")
                );

        // 4. Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                credentials.getHashedPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // 5. Generate JWT and return
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
        return null;
    }

    // Mapping User + Addresses
    private UserResponse mapToUserResponse(User user) {
        Set<AddressResponse> addressResponses = user.getAddresses()
                .stream()
                .map(a -> AddressResponse.builder()
                        .addressType(a.getAddressType())
                        .street(a.getStreet())
                        .street2(a.getStreet2())
                        .city(a.getCity())
                        .state(a.getState())
                        .zip(a.getZip())
                        .country(a.getCountry())
                        .isDefault(a.getIsDefault())
                        .build())
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .timezone(user.getTimeZone())
                .preferredLanguage(user.getPreferredLanguage())
                .accountStatus(user.getAccountStatus())
                .role(user.getRole())
                .addresses(addressResponses)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

    }
}

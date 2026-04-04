package com.rish.masterdata.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_credentials", schema = "masterdata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;  // LOCAL, GITHUB, GOOGLE

    private String providerUserId;      // OAuth provider's user ID

    private Integer failedLoginAttempts;

    private LocalDateTime lastLoginAt;

    private LocalDateTime passwordChangedAt;

    private LocalDateTime lockedAt;     // when account was locked

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        failedLoginAttempts = 0;
        authProvider = AuthProvider.LOCAL;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
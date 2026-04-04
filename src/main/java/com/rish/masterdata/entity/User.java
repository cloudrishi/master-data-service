package com.rish.masterdata.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", schema="masterdata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String timeZone;

    private String preferredLanguage;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user",
                cascade = CascadeType.ALL,
                fetch = FetchType.LAZY)
    private Set<Address> addresses = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // never changes after insert

    @Column(nullable = false)
    private LocalDateTime updatedAt;    // changes on every update

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        accountStatus = AccountStatus.ACTIVE;
        role = Role.USER;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

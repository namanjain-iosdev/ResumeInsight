package com.cvanalyzer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * Authentication source: LOCAL (email+password) or GOOGLE (OAuth2).
     * Nullable at the DB level so adding this column to an existing populated
     * users table via ddl-auto=update doesn't fail; defaulted to LOCAL in code.
     */
    @Column
    @Builder.Default
    private String provider = "LOCAL";

    /** Google subject id ("sub") for users who signed in with Google. */
    @Column
    private String googleId;

    /** Profile picture URL supplied by the OAuth provider. */
    @Column
    private String picture;

    /** Timestamp of the most recent successful login. */
    @Column
    private LocalDateTime lastLoginAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column
    private String emailVerificationToken;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

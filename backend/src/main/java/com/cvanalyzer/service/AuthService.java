package com.cvanalyzer.service;

import com.cvanalyzer.dto.*;
import com.cvanalyzer.entity.Role;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.entity.PasswordResetToken;
import com.cvanalyzer.exception.BadRequestException;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.repository.PasswordResetTokenRepository;
import com.cvanalyzer.repository.RoleRepository;
import com.cvanalyzer.repository.UserRepository;
import com.cvanalyzer.security.GoogleTokenVerifier;
import com.cvanalyzer.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(Role.RoleName.ROLE_USER).build()));

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

        user = userRepository.save(user);
        emailService.sendEmailVerification(user.getEmail(), user.getFullName(), verificationToken);

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return buildAuthResponse(user, token);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return buildAuthResponse(user, token);
    }

    /**
     * Authenticate via a verified Google ID token. Creates the user on first
     * sign-in, otherwise updates the last-login timestamp. Always issues a JWT.
     */
    @Transactional
    public AuthResponse googleLogin(String credential) {
        GoogleTokenVerifier.GoogleUser googleUser = googleTokenVerifier.verify(credential);

        User user = userRepository.findByEmail(googleUser.getEmail())
                .map(existing -> linkAndUpdate(existing, googleUser))
                .orElseGet(() -> createGoogleUser(googleUser));

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return buildAuthResponse(user, token);
    }

    private User linkAndUpdate(User user, GoogleTokenVerifier.GoogleUser googleUser) {
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleUser.getSub());
            user.setProvider("GOOGLE");
        }
        if (googleUser.getPicture() != null) {
            user.setPicture(googleUser.getPicture());
        }
        if (googleUser.isEmailVerified()) {
            user.setEmailVerified(true);
        }
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User createGoogleUser(GoogleTokenVerifier.GoogleUser googleUser) {
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(Role.RoleName.ROLE_USER).build()));

        User user = User.builder()
                .fullName(googleUser.getName() != null ? googleUser.getName() : googleUser.getEmail())
                .email(googleUser.getEmail())
                // Google users never authenticate with a password; store an
                // unusable random one so the non-null column stays satisfied.
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .provider("GOOGLE")
                .googleId(googleUser.getSub())
                .picture(googleUser.getPicture())
                .emailVerified(googleUser.isEmailVerified())
                .enabled(true)
                .lastLoginAt(LocalDateTime.now())
                .roles(Set.of(userRole))
                .build();

        user = userRepository.save(user);
        log.info("Created new user via Google login: {}", user.getEmail());
        return user;
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        passwordResetTokenRepository.deleteAllByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordReset(user.getEmail(), user.getFullName(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Reset token has already been used");
        }
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roles(roles)
                .emailVerified(user.isEmailVerified())
                .picture(user.getPicture())
                .provider(user.getProvider())
                .build();
    }
}

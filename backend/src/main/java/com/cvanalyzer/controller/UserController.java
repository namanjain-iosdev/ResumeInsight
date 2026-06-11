package com.cvanalyzer.controller;

import com.cvanalyzer.dto.ApiResponse;
import com.cvanalyzer.dto.UserResponse;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.repository.AnalysisRepository;
import com.cvanalyzer.repository.ResumeRepository;
import com.cvanalyzer.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final AnalysisRepository analysisRepository;

    @GetMapping("/me")
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", mapToResponse(user)));
    }

    @PutMapping("/me/name")
    @Operation(summary = "Update current user's full name")
    public ResponseEntity<ApiResponse<UserResponse>> updateName(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String fullName) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFullName(fullName);
        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Name updated", mapToResponse(user)));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .resumeCount(resumeRepository.countByUser(user))
                .analysisCount(analysisRepository.countByUser(user))
                .build();
    }
}

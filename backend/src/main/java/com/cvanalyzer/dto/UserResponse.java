package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private boolean emailVerified;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private long resumeCount;
    private long analysisCount;
}

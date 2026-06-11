package com.cvanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
    /** The Google ID token (JWT credential) returned by Google Identity Services. */
    @NotBlank(message = "Google credential is required")
    private String credential;
}

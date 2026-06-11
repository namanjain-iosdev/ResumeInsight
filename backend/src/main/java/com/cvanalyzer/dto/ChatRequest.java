package com.cvanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank
    private String message;
    private String chatType = "GENERAL"; // GENERAL, INTERVIEW_PREP, CAREER_GUIDANCE
    private Long resumeId;
}

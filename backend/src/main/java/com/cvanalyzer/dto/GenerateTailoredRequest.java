package com.cvanalyzer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateTailoredRequest {
    @NotNull(message = "resumeId is required")
    private Long resumeId;

    @NotBlank(message = "Job description is required")
    private String jobDescription;
}

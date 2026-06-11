package com.cvanalyzer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalyzeRequest {
    @NotNull
    private Long resumeId;
    private String jobDescription;
}

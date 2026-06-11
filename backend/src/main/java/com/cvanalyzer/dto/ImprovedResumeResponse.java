package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImprovedResumeResponse {
    private Long id;
    private Long analysisId;
    private String originalContent;
    private String improvedContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

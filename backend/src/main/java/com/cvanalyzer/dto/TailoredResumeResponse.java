package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TailoredResumeResponse {
    private Long id;
    private Long originalResumeId;
    private String originalFileName;
    private Integer versionNumber;
    private String jobDescription;
    private String originalContent;
    private String optimizedContent;
    private ChangeSummary changeSummary;
    private String aiProvider;
    private boolean validated;
    private boolean hasPdf;
    private LocalDateTime createdAt;
}

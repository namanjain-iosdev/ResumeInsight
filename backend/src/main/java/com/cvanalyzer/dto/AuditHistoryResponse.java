package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One row of the Audit History page: an uploaded resume together with its
 * analyses and any tailored resumes generated from it.
 */
@Data
@Builder
public class AuditHistoryResponse {
    private Long resumeId;
    private String originalFileName;
    private Integer versionNumber;
    private Long fileSize;
    private String checksum;
    private LocalDateTime uploadedAt;

    /** Latest analysis timestamp + provider, if the resume was analyzed. */
    private LocalDateTime lastAnalysisAt;
    private String analysisProvider;
    private int analysisCount;

    private List<GeneratedVersion> generatedVersions;

    @Data
    @Builder
    public static class GeneratedVersion {
        private Long id;
        private Integer versionNumber;
        private String aiProvider;
        private boolean validated;
        private boolean hasPdf;
        private LocalDateTime createdAt;
    }
}

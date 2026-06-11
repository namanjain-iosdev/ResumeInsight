package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResumeResponse {
    private Long id;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private Integer versionNumber;
    private String checksum;
    private LocalDateTime uploadedAt;
    private boolean hasAnalysis;
}

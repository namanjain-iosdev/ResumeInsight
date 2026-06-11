package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Long id;
    private String action;
    private Long resumeId;
    private String aiProvider;
    private String details;
    private LocalDateTime createdAt;
}

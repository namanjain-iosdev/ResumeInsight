package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalysisResponse {
    private Long id;
    private Long resumeId;
    private String resumeFileName;
    private Integer atsScore;
    private String technicalSkills;
    private String softSkills;
    private String missingKeywords;
    private String formattingIssues;
    private String experienceSummary;
    private String grammarSuggestions;
    private String industryAlignment;
    private String recommendations;
    private String jobDescription;
    private String overallFeedback;
    private String aiProvider;
    private LocalDateTime createdAt;
}

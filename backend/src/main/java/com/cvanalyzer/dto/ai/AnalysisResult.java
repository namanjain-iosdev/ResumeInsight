package com.cvanalyzer.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Structured shape of a resume-analysis AI response. Populated by
 * {@link com.cvanalyzer.service.ai.StructuredOutputService}; missing fields stay null.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalysisResult {
    private Integer atsScore;
    private String technicalSkills;
    private String softSkills;
    private String missingKeywords;
    private String formattingIssues;
    private String experienceSummary;
    private String grammarSuggestions;
    private String industryAlignment;
    private String recommendations;
    private String overallFeedback;
}

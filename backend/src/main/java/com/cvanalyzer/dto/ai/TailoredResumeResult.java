package com.cvanalyzer.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured AI output for a job-description-tailored resume. The model returns
 * the full optimized resume text plus an explicit, machine-readable summary of
 * what it changed (used by the comparison view, Feature 8).
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TailoredResumeResult {

    /** The complete optimized resume as plain text, line-by-line. */
    private String optimizedResume;

    /** Section names that were reordered relative to the original. */
    private List<String> reorderedSections = new ArrayList<>();

    /** Section / bullet descriptions that were rewritten for clarity or impact. */
    private List<String> rewrittenSections = new ArrayList<>();

    /** Keywords surfaced from the resume to better match the job description. */
    private List<String> keywordsEmphasized = new ArrayList<>();

    /** ATS-oriented improvements applied (formatting, phrasing, ordering). */
    private List<String> atsImprovements = new ArrayList<>();
}

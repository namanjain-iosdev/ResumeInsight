package com.cvanalyzer.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI grounding-verification verdict: whether the optimized resume introduced
 * any fact (skill, certification, project, experience, achievement, number)
 * that is not present in the original resume.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroundingCheckResult {
    /** True if hallucinated / invented content was detected. */
    private boolean hallucinated;

    /** The specific invented items found (empty when grounded). */
    private List<String> fabricatedItems = new ArrayList<>();
}

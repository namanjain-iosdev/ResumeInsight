package com.cvanalyzer.service;

import com.cvanalyzer.service.ai.StructuredOutputService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the deterministic layer of the no-hallucination guard. The AI layer is
 * exercised separately via {@link com.cvanalyzer.service.TailoredResumeService}.
 */
class ResumeGroundingValidatorTest {

    private final ResumeGroundingValidator validator =
            new ResumeGroundingValidator(new StructuredOutputService(new ObjectMapper()));

    @Test
    void flagsTechnologyNotInOriginal() {
        String original = "Software engineer skilled in Java and Spring Boot.";
        String optimized = "Software engineer skilled in Java, Spring Boot and Kubernetes (AWS certified).";
        List<String> issues = validator.deterministicIssues(original, optimized);
        // "AWS" acronym is not present in the original.
        assertTrue(issues.stream().anyMatch(i -> i.contains("AWS")), "Expected AWS to be flagged: " + issues);
    }

    @Test
    void allowsRewordingWithSameFacts() {
        String original = "Led a team of 5 engineers using Java and SQL.";
        String optimized = "Directed 5 engineers, delivering solutions with Java and SQL.";
        List<String> issues = validator.deterministicIssues(original, optimized);
        assertTrue(issues.isEmpty(), "Rewording with same facts should not be flagged: " + issues);
    }

    @Test
    void flagsInventedNumber() {
        String original = "Improved performance significantly across services.";
        String optimized = "Improved performance by 47% across 12 services.";
        List<String> issues = validator.deterministicIssues(original, optimized);
        assertFalse(issues.isEmpty(), "Invented metrics should be flagged");
    }
}

package com.cvanalyzer.service.ai;

import com.cvanalyzer.dto.ai.AnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructuredOutputServiceTest {

    private final StructuredOutputService service = new StructuredOutputService(new ObjectMapper());

    @Test
    void parsesCleanJson() {
        String raw = "{\"atsScore\": 88, \"technicalSkills\": \"Java, Spring\"}";
        AnalysisResult r = service.parse(raw, AnalysisResult.class);
        assertEquals(88, r.getAtsScore());
        assertEquals("Java, Spring", r.getTechnicalSkills());
    }

    @Test
    void parsesJsonWrappedInProseAndFences() {
        String raw = "Sure!\n```json\n{\"atsScore\": 72, \"overallFeedback\": \"solid\"}\n```";
        AnalysisResult r = service.parse(raw, AnalysisResult.class);
        assertEquals(72, r.getAtsScore());
        assertEquals("solid", r.getOverallFeedback());
    }

    @Test
    void repairsTrailingCommaAndParses() {
        String raw = "{\"atsScore\": 50, \"softSkills\": \"teamwork\",}";
        AnalysisResult r = service.parse(raw, AnalysisResult.class);
        assertEquals(50, r.getAtsScore());
        assertEquals("teamwork", r.getSoftSkills());
    }

    @Test
    void toleratesMissingFields() {
        AnalysisResult r = service.parse("{\"atsScore\": 30}", AnalysisResult.class);
        assertEquals(30, r.getAtsScore());
        assertNull(r.getTechnicalSkills());
    }

    @Test
    void throwsOnUnrecoverableContent() {
        assertThrows(StructuredOutputService.StructuredOutputException.class,
                () -> service.parse("totally not json at all", AnalysisResult.class));
    }
}

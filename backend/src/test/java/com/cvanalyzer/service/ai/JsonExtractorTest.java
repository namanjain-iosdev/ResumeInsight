package com.cvanalyzer.service.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonExtractorTest {

    @Test
    void extractsJsonFromMarkdownFence() {
        String raw = "Here you go:\n```json\n{\"a\": 1}\n```\nThanks!";
        assertEquals("{\"a\": 1}", JsonExtractor.extract(raw).trim());
    }

    @Test
    void extractsJsonFromSurroundingProse() {
        String raw = "Sure! {\"atsScore\": 80, \"notes\": \"good\"} hope that helps";
        String json = JsonExtractor.extract(raw);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("atsScore"));
    }

    @Test
    void repairRemovesTrailingCommas() {
        String repaired = JsonExtractor.repair("{\"a\": 1, \"b\": 2,}");
        assertEquals("{\"a\": 1, \"b\": 2}", repaired);
    }

    @Test
    void repairBalancesTruncatedObject() {
        String repaired = JsonExtractor.repair("{\"a\": 1, \"b\": {\"c\": 2");
        // Two opens, zero/one close -> braces should balance out.
        long opens = repaired.chars().filter(ch -> ch == '{').count();
        long closes = repaired.chars().filter(ch -> ch == '}').count();
        assertEquals(opens, closes);
    }

    @Test
    void repairNormalizesSmartQuotes() {
        String repaired = JsonExtractor.repair("{“key”: “value”}");
        assertTrue(repaired.contains("\"key\""));
        assertTrue(repaired.contains("\"value\""));
    }
}

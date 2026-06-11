package com.cvanalyzer.service;

import com.cvanalyzer.dto.ai.GroundingCheckResult;
import com.cvanalyzer.service.ai.AIProvider;
import com.cvanalyzer.service.ai.AiMessage;
import com.cvanalyzer.service.ai.AiRequest;
import com.cvanalyzer.service.ai.StructuredOutputService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enforces the critical no-hallucination requirement (Feature 3): every fact in
 * the optimized resume must originate from the original resume.
 *
 * <p>Two complementary layers:
 * <ol>
 *   <li><b>Deterministic check</b> — extracts high-risk "claim tokens"
 *       (numbers/percentages, acronyms, capitalised technology-like terms) from
 *       the optimized text and flags any that do not appear in the original.</li>
 *   <li><b>AI verification pass</b> — a second model call that explicitly looks
 *       for invented skills, certifications, projects, experience or
 *       achievements.</li>
 * </ol>
 * If either layer flags content, generation is rejected so the caller can
 * regenerate more conservatively.
 */
@Service
@Slf4j
public class ResumeGroundingValidator {

    private final StructuredOutputService structuredOutputService;

    public ResumeGroundingValidator(StructuredOutputService structuredOutputService) {
        this.structuredOutputService = structuredOutputService;
    }

    private static final Pattern NUMBER = Pattern.compile("\\b\\d[\\d.,]*%?\\+?\\b");
    private static final Pattern ACRONYM = Pattern.compile("\\b[A-Z]{2,6}(?:\\+\\+|#|\\.js)?\\b");
    // Stopwords that are commonly capitalised at line starts but aren't claims.
    private static final Set<String> STOP = new HashSet<>(Arrays.asList(
            "THE", "AND", "FOR", "WITH", "FROM", "THIS", "THAT", "YEARS", "YEAR",
            "EXPERIENCE", "SKILLS", "EDUCATION", "SUMMARY", "PROJECTS", "OBJECTIVE"));

    @Data
    public static class ValidationOutcome {
        private final boolean grounded;
        private final List<String> issues;
    }

    /** Run both layers. {@code grounded == true} means safe to keep. */
    public ValidationOutcome validate(String originalText, String optimizedText, AIProvider provider) {
        List<String> issues = new ArrayList<>(deterministicIssues(originalText, optimizedText));

        try {
            GroundingCheckResult ai = aiVerify(originalText, optimizedText, provider);
            if (ai.isHallucinated() && ai.getFabricatedItems() != null) {
                issues.addAll(ai.getFabricatedItems());
            } else if (ai.isHallucinated()) {
                issues.add("AI grounding check flagged unverifiable content");
            }
        } catch (Exception e) {
            // If the AI verifier itself fails, fall back to the deterministic result only.
            log.warn("AI grounding verification failed, using deterministic check only: {}", e.getMessage());
        }

        return new ValidationOutcome(issues.isEmpty(), issues);
    }

    /** Deterministic detection of numbers/acronyms present in optimized but not original. */
    public List<String> deterministicIssues(String originalText, String optimizedText) {
        String normOriginal = normalize(originalText);
        Set<String> originalTokens = new HashSet<>(Arrays.asList(normOriginal.split("\\s+")));

        List<String> issues = new ArrayList<>();

        // Numbers / quantified claims must be traceable to the original.
        Matcher num = NUMBER.matcher(optimizedText);
        while (num.find()) {
            String token = num.group();
            String digits = token.replaceAll("[^0-9]", "");
            if (digits.length() >= 2 && !normOriginal.contains(digits)) {
                issues.add("Number not in original: " + token);
            }
        }

        // Acronyms / technologies must already be present.
        Matcher acr = ACRONYM.matcher(optimizedText);
        while (acr.find()) {
            String token = acr.group();
            if (STOP.contains(token.toUpperCase())) continue;
            String norm = normalizeToken(token);
            if (!norm.isBlank() && !originalTokens.contains(norm) && !normOriginal.contains(norm)) {
                issues.add("Term not in original: " + token);
            }
        }
        // De-duplicate while keeping order.
        return new ArrayList<>(new java.util.LinkedHashSet<>(issues));
    }

    private GroundingCheckResult aiVerify(String originalText, String optimizedText, AIProvider provider) {
        String prompt = "You are a strict fact-checker. Compare the OPTIMIZED resume against the ORIGINAL resume.\n" +
                "Flag ANY skill, technology, certification, project, employer, job title, degree, date, metric, or " +
                "achievement that appears in the OPTIMIZED resume but is NOT supported by the ORIGINAL resume. " +
                "Rewording, reordering and rephrasing are allowed and must NOT be flagged.\n\n" +
                "Return ONLY this JSON: {\"hallucinated\": <true|false>, \"fabricatedItems\": [\"...\"]}\n\n" +
                "ORIGINAL RESUME:\n" + originalText + "\n\nOPTIMIZED RESUME:\n" + optimizedText;

        AiRequest request = AiRequest.builder()
                .message(AiMessage.system("You verify that resumes contain no invented information. Be precise and conservative."))
                .message(AiMessage.user(prompt))
                .temperature(0.0)
                .maxTokens(800)
                .jsonMode(true)
                .build();

        return structuredOutputService.generate(provider, request, GroundingCheckResult.class);
    }

    private String normalize(String text) {
        if (text == null) return "";
        String n = Normalizer.normalize(text, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        // Keep alphanumerics and a few tech symbols, collapse the rest to spaces.
        return n.replaceAll("[^a-z0-9+#. ]", " ").replaceAll("\\s+", " ").trim();
    }

    private String normalizeToken(String token) {
        return normalize(token).trim();
    }
}

package com.cvanalyzer.service.ai;

import com.cvanalyzer.exception.AiProviderException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared implementation for any provider that speaks the OpenAI
 * {@code /chat/completions} protocol. Both {@link OpenAIService} and
 * {@link LMStudioService} extend this, so prompt building, request
 * serialization and response parsing live in exactly one place.
 *
 * <p>
 * Subclasses only supply endpoint/credentials/model details and prompt
 * wording differences.
 */
@Slf4j
public abstract class AbstractOpenAiCompatibleProvider implements AIProvider {

    protected final RestTemplate restTemplate = new RestTemplate();

    // ── Subclass hooks ──────────────────────────────────────────────────────
    protected abstract String getBaseUrl();

    protected abstract String getModel();

    /** Default system prompt used by the convenience helpers. */
    protected String defaultSystemPrompt() {
        return "You are a professional resume analyst and career coach.";
    }

    /** Allow subclasses (e.g. OpenAI) to add auth headers. */
    protected void customizeHeaders(HttpHeaders headers) {
        // no-op by default
    }

    // ── Core completion ─────────────────────────────────────────────────────
    @Override
    @SuppressWarnings("unchecked")
    public AiCompletion complete(AiRequest request) {
        try {
            String url = getBaseUrl() + "/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            customizeHeaders(headers);

            Map<String, Object> body = new HashMap<>();
            body.put("model", getModel());
            body.put("max_tokens", request.getMaxTokens());
            body.put("temperature", request.getTemperature());
            Map<String, Object> responseFormat = getResponseFormat(request.isJsonMode());

            if (responseFormat != null) {
                body.put("response_format", responseFormat);
            }
            List<Map<String, String>> messages = new ArrayList<>();
            for (AiMessage m : request.getMessages()) {
                messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
            }
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            String content = extractContent(response.getBody());
            return AiCompletion.builder()
                    .content(content)
                    .provider(getProviderName())
                    .model(getModel())
                    .build();
        } catch (RestClientResponseException e) {
            // The provider returned an HTTP error (4xx/5xx) — translate the most
            // common cases into an actionable message.
            log.error("{} API call failed: {} {}", getProviderName(), e.getStatusText(), e.getResponseBodyAsString());
            throw new AiProviderException(describeHttpError(e), e);
        } catch (AiProviderException e) {
            throw e;
        } catch (Exception e) {
            log.error("{} API call failed", getProviderName(), e);
            throw new AiProviderException(
                    "The AI provider (" + getProviderName() + ") is unreachable: " + e.getMessage(), e);
        }
    }

    private String describeHttpError(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();
        String provider = getProviderName();
        if (status == 429 || (body != null && body.contains("insufficient_quota"))) {
            return "The AI provider (" + provider + ") rejected the request: quota exceeded or rate-limited. " +
                    "Check the provider's billing/usage, or set AI_PROVIDER to a working provider.";
        }
        if (status == 401 || status == 403) {
            return "The AI provider (" + provider + ") rejected the credentials. Verify the API key configuration.";
        }
        if (status == 404) {
            return "The AI provider (" + provider
                    + ") could not find the requested model. Verify the configured model name.";
        }
        return "The AI provider (" + provider + ") returned an error (HTTP " + status + ").";
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> responseBody) {
        if (responseBody == null) {
            throw new RuntimeException("Empty response body from provider");
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in provider response");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null || message.get("content") == null) {
            throw new RuntimeException("No message content in provider response");
        }
        return (String) message.get("content");
    }

    protected String complete(String systemPrompt, String userPrompt) {
        return complete(AiRequest.of(systemPrompt, userPrompt)).getContent();
    }

    // ── Feature helpers (shared prompts) ────────────────────────────────────
    @Override
    public String analyzeResume(String resumeText, String jobDescription) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze the following resume and provide a comprehensive evaluation. ")
                .append("Return ONLY a JSON object with these EXACT fields:\n")
                .append("{\n")
                .append("  \"atsScore\": <integer 0-100>,\n")
                .append("  \"technicalSkills\": \"comma-separated technical skills found\",\n")
                .append("  \"softSkills\": \"comma-separated soft skills found\",\n")
                .append("  \"missingKeywords\": \"comma-separated important missing keywords\",\n")
                .append("  \"formattingIssues\": \"formatting issues found\",\n")
                .append("  \"experienceSummary\": \"brief summary of experience\",\n")
                .append("  \"grammarSuggestions\": \"grammar improvement suggestions\",\n")
                .append("  \"industryAlignment\": \"industry alignment assessment\",\n")
                .append("  \"recommendations\": \"top improvement recommendations\",\n")
                .append("  \"overallFeedback\": \"overall feedback paragraph\"\n")
                .append("}\n\nRESUME:\n").append(resumeText);
        if (jobDescription != null && !jobDescription.isBlank()) {
            prompt.append("\n\nJOB DESCRIPTION (match against this):\n").append(jobDescription);
        }
        return complete(AiRequest.builder()
                .message(AiMessage.system(defaultSystemPrompt()))
                .message(AiMessage.user(prompt.toString()))
                .jsonMode(true)
                .build()).getContent();
    }

    @Override
    public String improveResume(String resumeText) {
        String prompt = "You are a professional resume writer and career coach. Improve the following resume by:\n" +
                "1. Enhancing the professional summary\n" +
                "2. Improving bullet points with strong action verbs and quantifiable results\n" +
                "3. Optimizing for ATS systems\n" +
                "4. Improving the skills section\n" +
                "5. Fixing any grammar or formatting issues\n\n" +
                "Return the complete improved resume in clean, professional format.\n\n" +
                "ORIGINAL RESUME:\n" + resumeText;
        return complete(defaultSystemPrompt(), prompt);
    }

    @Override
    public String chat(String message, String chatType, String resumeContext) {
        String systemPrompt = buildChatSystemPrompt(chatType, resumeContext);
        return complete(systemPrompt, message);
    }

    @Override
    public String generateInterviewQuestions(String resumeText, String role) {
        String prompt = "Based on the following resume, generate 10 targeted interview questions for a " + role +
                " position. Include both technical and behavioral questions. Format as a numbered list with brief " +
                "tips for answering each.\n\nResume:\n" + resumeText;
        return complete(defaultSystemPrompt(), prompt);
    }

    private String buildChatSystemPrompt(String chatType, String resumeContext) {
        String base = "You are an expert career coach and resume specialist. ";
        return switch (chatType == null ? "" : chatType) {
            case "INTERVIEW_PREP" ->
                base + "Help the user prepare for job interviews with specific tips and practice questions. " +
                        (resumeContext != null
                                ? "Their resume context: "
                                        + resumeContext.substring(0, Math.min(500, resumeContext.length()))
                                : "");
            case "CAREER_GUIDANCE" ->
                base + "Provide career guidance, growth strategies, and professional development advice.";
            default -> base + "Help with resume writing, job searching, and career-related questions.";
        };
    }

    protected Map<String, Object> getResponseFormat(boolean jsonMode) {
        if (!jsonMode) {
            return null;
        }

        // OpenAI default
        return Map.of("type", "json_object");
    }
}

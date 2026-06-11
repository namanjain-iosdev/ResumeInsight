package com.cvanalyzer.service.ai;

import com.cvanalyzer.exception.AiProviderException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

/**
 * Robust structured-output layer (Feature 6).
 *
 * <pre>
 *   AI response -> extract JSON -> validate -> repair if malformed
 *               -> deserialize to DTO -> retry on failure -> structured response
 * </pre>
 *
 * Works identically for OpenAI and LM Studio because it operates only on the
 * provider-agnostic {@link AiCompletion}. Missing fields are tolerated (they
 * map to null); a caller-supplied validator decides whether a parse is
 * acceptable, and the request is re-issued (with a stricter reminder) when it
 * is not.
 */
@Service
@Slf4j
public class StructuredOutputService {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final ObjectMapper objectMapper;

    public StructuredOutputService(ObjectMapper objectMapper) {
        // A lenient copy so unknown / missing fields never break deserialization.
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public <T> T generate(AIProvider provider, AiRequest request, Class<T> type) {
        return generate(provider, request, type, t -> true, DEFAULT_MAX_ATTEMPTS);
    }

    public <T> T generate(AIProvider provider, AiRequest request, Class<T> type, Predicate<T> validator) {
        return generate(provider, request, type, validator, DEFAULT_MAX_ATTEMPTS);
    }

    /**
     * Force JSON mode, call the provider, and parse into {@code type}. Retries
     * up to {@code maxAttempts} times when extraction, deserialization, or
     * {@code validator} fails — appending a corrective instruction each retry.
     */
    public <T> T generate(AIProvider provider, AiRequest request, Class<T> type,
                          Predicate<T> validator, int maxAttempts) {
        AiRequest jsonRequest = ensureJsonMode(request);
        RuntimeException last = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            AiRequest attemptRequest = attempt == 1
                    ? jsonRequest
                    : withCorrection(jsonRequest, last);
            try {
                AiCompletion completion = provider.complete(attemptRequest);
                T parsed = parse(completion.getContent(), type);
                if (!validator.test(parsed)) {
                    throw new StructuredOutputException("Validation rejected the parsed response");
                }
                return parsed;
            } catch (AiProviderException e) {
                // Upstream provider failure (quota, auth, unreachable) — retrying
                // won't help, so surface it immediately with its actionable message.
                throw e;
            } catch (RuntimeException e) {
                last = e instanceof StructuredOutputException
                        ? e : new StructuredOutputException(e.getMessage(), e);
                log.warn("Structured output attempt {}/{} failed: {}", attempt, maxAttempts, e.getMessage());
            }
        }
        throw new StructuredOutputException(
                "Failed to obtain valid structured output after " + maxAttempts + " attempts", last);
    }

    /** Parse a single AI response string into a DTO (extract -> repair -> deserialize). */
    public <T> T parse(String rawContent, Class<T> type) {
        String json = JsonExtractor.extract(rawContent);
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception first) {
            // Second chance: repair then retry.
            try {
                String repaired = JsonExtractor.repair(json);
                return objectMapper.readValue(repaired, type);
            } catch (Exception second) {
                throw new StructuredOutputException(
                        "Could not deserialize AI response into " + type.getSimpleName()
                                + ": " + second.getMessage(), second);
            }
        }
    }

    private AiRequest ensureJsonMode(AiRequest request) {
        if (request.isJsonMode()) return request;
        return AiRequest.builder()
                .messages(request.getMessages())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .jsonMode(true)
                .build();
    }

    private AiRequest withCorrection(AiRequest request, RuntimeException previousError) {
        AiRequest.AiRequestBuilder b = AiRequest.builder()
                .temperature(Math.max(0.0, request.getTemperature() - 0.2))
                .maxTokens(request.getMaxTokens())
                .jsonMode(true);
        request.getMessages().forEach(b::message);
        b.message(AiMessage.user(
                "Your previous response was not valid JSON (" +
                        (previousError == null ? "unknown error" : previousError.getMessage()) +
                        "). Respond again with ONLY a single, complete, valid JSON object and nothing else."));
        return b.build();
    }

    public static class StructuredOutputException extends RuntimeException {
        public StructuredOutputException(String message) { super(message); }
        public StructuredOutputException(String message, Throwable cause) { super(message, cause); }
    }
}

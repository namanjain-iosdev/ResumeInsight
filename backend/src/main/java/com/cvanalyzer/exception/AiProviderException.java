package com.cvanalyzer.exception;

/**
 * Raised when an upstream AI provider (OpenAI / LM Studio) request fails —
 * e.g. quota exceeded, invalid key, model not loaded, or the provider being
 * unreachable. Carries a user-actionable message and is surfaced as HTTP 502
 * rather than a generic 500.
 */
public class AiProviderException extends RuntimeException {
    public AiProviderException(String message) {
        super(message);
    }

    public AiProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}

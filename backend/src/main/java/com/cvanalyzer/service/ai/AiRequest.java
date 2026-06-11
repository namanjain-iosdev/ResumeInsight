package com.cvanalyzer.service.ai;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * Provider-agnostic chat-completion request. Business logic builds one of
 * these and never touches provider-specific payload structures.
 */
@Data
@Builder
public class AiRequest {

    @Singular
    private List<AiMessage> messages;

    @Builder.Default
    private double temperature = 0.7;

    @Builder.Default
    private int maxTokens = 2000;

    /**
     * When true, the provider is asked to return strict JSON. OpenAI supports
     * response_format={type:json_object}; LM Studio honours it when the loaded
     * model supports it and otherwise ignores it harmlessly.
     */
    @Builder.Default
    private boolean jsonMode = false;

    public static AiRequest of(String systemPrompt, String userPrompt) {
        return AiRequest.builder()
                .message(AiMessage.system(systemPrompt))
                .message(AiMessage.user(userPrompt))
                .build();
    }
}

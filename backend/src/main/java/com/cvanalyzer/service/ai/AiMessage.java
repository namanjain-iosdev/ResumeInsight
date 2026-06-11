package com.cvanalyzer.service.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Provider-agnostic chat message. Mirrors the OpenAI-compatible
 * {role, content} shape that both OpenAI and LM Studio understand.
 */
@Data
@AllArgsConstructor
public class AiMessage {
    private String role;
    private String content;

    public static AiMessage system(String content) {
        return new AiMessage("system", content);
    }

    public static AiMessage user(String content) {
        return new AiMessage("user", content);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage("assistant", content);
    }
}

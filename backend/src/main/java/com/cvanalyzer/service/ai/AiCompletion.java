package com.cvanalyzer.service.ai;

import lombok.Builder;
import lombok.Data;

/**
 * Provider-agnostic chat-completion response. Business logic reads
 * {@link #content} regardless of which provider produced it.
 */
@Data
@Builder
public class AiCompletion {
    private String content;
    private String provider;
    private String model;
}

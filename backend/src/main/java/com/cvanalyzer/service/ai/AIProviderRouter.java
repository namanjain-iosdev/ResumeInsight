package com.cvanalyzer.service.ai;

import com.cvanalyzer.config.AIProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AIProviderRouter {

    private final AIProperties aiProperties;
    private final OpenAIService openAIService;
    private final LMStudioService lmStudioService;

    public AIProviderRouter(AIProperties aiProperties,
                            OpenAIService openAIService,
                            LMStudioService lmStudioService) {
        this.aiProperties = aiProperties;
        this.openAIService = openAIService;
        this.lmStudioService = lmStudioService;
    }

    public AIProvider getProvider() {
        String configured = aiProperties.getProvider();
        if ("lmstudio".equalsIgnoreCase(configured)) {
            if (lmStudioService.isAvailable()) {
                log.info("Using LM Studio provider");
                return lmStudioService;
            }
            log.warn("LM Studio not available, falling back to OpenAI");
        }
        if (openAIService.isAvailable()) {
            log.info("Using OpenAI provider");
            return openAIService;
        }
        if (lmStudioService.isAvailable()) {
            log.info("Falling back to LM Studio provider");
            return lmStudioService;
        }
        throw new RuntimeException("No AI provider is available. Please configure OpenAI API key or start LM Studio.");
    }

    public String getActiveProviderName() {
        try {
            return getProvider().getProviderName();
        } catch (Exception e) {
            return "none";
        }
    }

    public boolean isAnyProviderAvailable() {
        return openAIService.isAvailable() || lmStudioService.isAvailable();
    }
}

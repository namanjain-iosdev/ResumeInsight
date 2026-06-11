package com.cvanalyzer.service.ai;

import com.cvanalyzer.config.AIProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
public class OpenAIService extends AbstractOpenAiCompatibleProvider {

    private final AIProperties aiProperties;
    private static final String PROVIDER_NAME = "openai";

    public OpenAIService(AIProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    protected String getBaseUrl() {
        return aiProperties.getOpenai().getBaseUrl();
    }

    @Override
    protected String getModel() {
        return aiProperties.getOpenai().getModel();
    }

    @Override
    protected void customizeHeaders(HttpHeaders headers) {
        headers.setBearerAuth(aiProperties.getOpenai().getApiKey());
    }

    @Override
    public boolean isAvailable() {
        String key = aiProperties.getOpenai().getApiKey();
        return key != null && !key.isBlank();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
protected Map<String, Object> getResponseFormat(boolean jsonMode) {
    if (!jsonMode) {
        return null;
    }

    return Map.of("type", "json_object");
}
}

package com.cvanalyzer.service.ai;

import com.cvanalyzer.config.AIProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
public class LMStudioService extends AbstractOpenAiCompatibleProvider {

    private final AIProperties aiProperties;
    private static final String PROVIDER_NAME = "lmstudio";

    public LMStudioService(AIProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @Override
    protected String getBaseUrl() {
        return aiProperties.getLmstudio().getBaseUrl();
    }

    @Override
    protected String getModel() {
        return aiProperties.getLmstudio().getModel();
    }

    @Override
    public boolean isAvailable() {
        try {
            String url = aiProperties.getLmstudio().getBaseUrl() + "/models";
            restTemplate.getForEntity(url, String.class);
            return true;
        } catch (Exception e) {
            log.warn("LM Studio not available: {}", e.getMessage());
            return false;
        }
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

        // LM Studio doesn't support json_object
        return Map.of("type", "text");
    }
}

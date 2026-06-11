package com.cvanalyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
@Data
public class AIProperties {
    private String provider;

    private OpenAI openai = new OpenAI();
    private LmStudio lmstudio = new LmStudio();

    @Data
    public static class OpenAI {
        private String apiKey;
        private String model;
        private String baseUrl;
    }

    @Data
    public static class LmStudio {
        private String baseUrl;
        private String model;
    }
}

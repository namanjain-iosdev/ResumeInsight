package com.cvanalyzer.service.ai;

public interface AIProvider {

    /**
     * Provider-agnostic chat completion. This is the single low-level entry
     * point; all feature-specific helpers below are built on top of it so that
     * business logic never depends on provider-specific JSON structures.
     */
    AiCompletion complete(AiRequest request);

    String analyzeResume(String resumeText, String jobDescription);
    String improveResume(String resumeText);
    String chat(String message, String chatType, String resumeContext);
    String generateInterviewQuestions(String resumeText, String role);
    boolean isAvailable();
    String getProviderName();
}

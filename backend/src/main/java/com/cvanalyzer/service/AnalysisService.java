package com.cvanalyzer.service;

import com.cvanalyzer.dto.AnalysisResponse;
import com.cvanalyzer.dto.AnalyzeRequest;
import com.cvanalyzer.dto.PageResponse;
import com.cvanalyzer.entity.Analysis;
import com.cvanalyzer.entity.Resume;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.exception.BadRequestException;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.repository.AnalysisRepository;
import com.cvanalyzer.repository.ResumeRepository;
import com.cvanalyzer.repository.UserRepository;
import com.cvanalyzer.dto.ai.AnalysisResult;
import com.cvanalyzer.service.ai.AIProvider;
import com.cvanalyzer.service.ai.AIProviderRouter;
import com.cvanalyzer.service.ai.StructuredOutputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AIProviderRouter aiProviderRouter;
    private final StructuredOutputService structuredOutputService;
    private final AuditService auditService;

    @Transactional
    public AnalysisResponse analyzeResume(AnalyzeRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("You don't have access to this resume");
        }

        String resumeText = resume.getExtractedText();
        if (resumeText == null || resumeText.isBlank()) {
            throw new BadRequestException("Resume text could not be extracted");
        }

        AIProvider provider = aiProviderRouter.getProvider();
        String aiResponse = provider.analyzeResume(resumeText, request.getJobDescription());
        log.info("AI analysis received from provider: {}", provider.getProviderName());

        Analysis analysis = parseAndSaveAnalysis(aiResponse, resume, user, request.getJobDescription(), provider.getProviderName());
        auditService.record(user, com.cvanalyzer.entity.AuditLog.AuditAction.RESUME_ANALYZE,
                resume.getId(), provider.getProviderName(),
                "Analyzed '" + resume.getOriginalFileName() + "' (ATS " + analysis.getAtsScore() + ")");
        return mapToResponse(analysis);
    }

    public PageResponse<AnalysisResponse> getUserAnalyses(String userEmail, int page, int size) {
        User user = getUserByEmail(userEmail);
        Page<Analysis> analysisPage = analysisRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size));
        List<AnalysisResponse> content = analysisPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return PageResponse.<AnalysisResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(analysisPage.getTotalElements())
                .totalPages(analysisPage.getTotalPages())
                .last(analysisPage.isLast())
                .build();
    }

    public AnalysisResponse getAnalysisById(Long analysisId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Analysis analysis = analysisRepository.findByIdAndUser(analysisId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + analysisId));
        return mapToResponse(analysis);
    }

    @Transactional
    public void deleteAnalysis(Long analysisId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Analysis analysis = analysisRepository.findByIdAndUser(analysisId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + analysisId));
        analysisRepository.delete(analysis);
    }

    private Analysis parseAndSaveAnalysis(String aiResponse, Resume resume, User user,
                                          String jobDescription, String providerName) {
        Analysis.AnalysisBuilder builder = Analysis.builder()
                .resume(resume)
                .user(user)
                .jobDescription(jobDescription)
                .aiProvider(providerName);

        try {
            AnalysisResult parsed = structuredOutputService.parse(aiResponse, AnalysisResult.class);
            builder.atsScore(parsed.getAtsScore())
                    .technicalSkills(parsed.getTechnicalSkills())
                    .softSkills(parsed.getSoftSkills())
                    .missingKeywords(parsed.getMissingKeywords())
                    .formattingIssues(parsed.getFormattingIssues())
                    .experienceSummary(parsed.getExperienceSummary())
                    .grammarSuggestions(parsed.getGrammarSuggestions())
                    .industryAlignment(parsed.getIndustryAlignment())
                    .recommendations(parsed.getRecommendations())
                    .overallFeedback(parsed.getOverallFeedback());
        } catch (Exception e) {
            log.warn("Could not parse JSON from AI response, storing raw response", e);
            builder.overallFeedback(aiResponse)
                    .atsScore(0);
        }

        return analysisRepository.save(builder.build());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private AnalysisResponse mapToResponse(Analysis analysis) {
        return AnalysisResponse.builder()
                .id(analysis.getId())
                .resumeId(analysis.getResume().getId())
                .resumeFileName(analysis.getResume().getOriginalFileName())
                .atsScore(analysis.getAtsScore())
                .technicalSkills(analysis.getTechnicalSkills())
                .softSkills(analysis.getSoftSkills())
                .missingKeywords(analysis.getMissingKeywords())
                .formattingIssues(analysis.getFormattingIssues())
                .experienceSummary(analysis.getExperienceSummary())
                .grammarSuggestions(analysis.getGrammarSuggestions())
                .industryAlignment(analysis.getIndustryAlignment())
                .recommendations(analysis.getRecommendations())
                .jobDescription(analysis.getJobDescription())
                .overallFeedback(analysis.getOverallFeedback())
                .aiProvider(analysis.getAiProvider())
                .createdAt(analysis.getCreatedAt())
                .build();
    }
}

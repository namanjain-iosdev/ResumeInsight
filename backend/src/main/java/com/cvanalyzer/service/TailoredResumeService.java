package com.cvanalyzer.service;

import com.cvanalyzer.dto.ChangeSummary;
import com.cvanalyzer.dto.TailoredResumeResponse;
import com.cvanalyzer.dto.ai.TailoredResumeResult;
import com.cvanalyzer.entity.AuditLog;
import com.cvanalyzer.entity.GeneratedResume;
import com.cvanalyzer.entity.Resume;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.exception.BadRequestException;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.repository.GeneratedResumeRepository;
import com.cvanalyzer.repository.ResumeRepository;
import com.cvanalyzer.repository.UserRepository;
import com.cvanalyzer.service.ai.AIProvider;
import com.cvanalyzer.service.ai.AIProviderRouter;
import com.cvanalyzer.service.ai.AiMessage;
import com.cvanalyzer.service.ai.AiRequest;
import com.cvanalyzer.service.ai.StructuredOutputService;
import com.cvanalyzer.service.pdf.FormattedPdfGenerator;
import com.cvanalyzer.service.pdf.PdfStyleAnalyzer;
import com.cvanalyzer.service.pdf.PdfStyleProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * "Generate Tailored Resume" feature (Feature 3) plus storage (7), comparison
 * metadata (8) and formatting preservation (4).
 *
 * <p>Flow: extract resume content → AI optimizes against the job description
 * with strict no-invention rules → grounding validation → regenerate if
 * hallucination detected → render a formatting-preserving PDF → store as a new
 * version linked to the original resume.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TailoredResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final AIProviderRouter aiProviderRouter;
    private final StructuredOutputService structuredOutputService;
    private final ResumeGroundingValidator groundingValidator;
    private final FileStorageService fileStorageService;
    private final PdfStyleAnalyzer pdfStyleAnalyzer;
    private final FormattedPdfGenerator formattedPdfGenerator;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    private static final int MAX_GENERATION_ATTEMPTS = 3;

    @Transactional
    public TailoredResumeResponse generate(Long resumeId, String jobDescription, String userEmail) {
        User user = getUser(userEmail);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));
        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new BadRequestException("You don't have access to this resume");
        }

        String originalText = resume.getExtractedText();
        if (originalText == null || originalText.isBlank()) {
            throw new BadRequestException("Resume text could not be extracted for optimization");
        }
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new BadRequestException("Job description is required");
        }

        AIProvider provider = aiProviderRouter.getProvider();

        TailoredResumeResult result = null;
        ResumeGroundingValidator.ValidationOutcome lastOutcome = null;

        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            TailoredResumeResult candidate = structuredOutputService.generate(
                    provider, buildGenerationRequest(originalText, jobDescription, lastOutcome), TailoredResumeResult.class,
                    r -> r.getOptimizedResume() != null && !r.getOptimizedResume().isBlank());

            ResumeGroundingValidator.ValidationOutcome outcome =
                    groundingValidator.validate(originalText, candidate.getOptimizedResume(), provider);

            if (outcome.isGrounded()) {
                result = candidate;
                lastOutcome = outcome;
                log.info("Tailored resume grounded on attempt {}", attempt);
                break;
            }
            lastOutcome = outcome;
            log.warn("Tailored resume attempt {}/{} rejected — hallucinated content: {}",
                    attempt, MAX_GENERATION_ATTEMPTS, outcome.getIssues());
        }

        if (result == null) {
            throw new BadRequestException(
                    "Could not generate a tailored resume without introducing unverifiable content. " +
                    "Detected issues: " + (lastOutcome != null ? lastOutcome.getIssues() : "unknown") +
                    ". Please try again or refine the job description.");
        }

        // Build change summary (Feature 8) from the structured AI output.
        ChangeSummary summary = new ChangeSummary();
        summary.setReorderedSections(safe(result.getReorderedSections()));
        summary.setRewrittenSections(safe(result.getRewrittenSections()));
        summary.setKeywordsEmphasized(safe(result.getKeywordsEmphasized()));
        summary.setAtsImprovements(safe(result.getAtsImprovements()));

        // Render a formatting-preserving PDF (Feature 4) and store it (Feature 7).
        String pdfPath = renderAndStorePdf(resume, result.getOptimizedResume());

        int version = (int) generatedResumeRepository.countByOriginalResumeId(resume.getId()) + 1;

        GeneratedResume generated = GeneratedResume.builder()
                .user(user)
                .originalResume(resume)
                .versionNumber(version)
                .jobDescription(jobDescription)
                .originalContent(originalText)
                .optimizedContent(result.getOptimizedResume())
                .changeSummary(writeJson(summary))
                .pdfPath(pdfPath)
                .aiProvider(provider.getProviderName())
                .validated(true)
                .build();
        generated = generatedResumeRepository.save(generated);

        auditService.record(user, AuditLog.AuditAction.TAILORED_RESUME_GENERATE,
                resume.getId(), provider.getProviderName(),
                "Generated tailored resume v" + version + " for '" + resume.getOriginalFileName() + "'");

        return mapToResponse(generated, summary);
    }

    public List<TailoredResumeResponse> getVersionsForResume(Long resumeId, String userEmail) {
        getUser(userEmail);
        return generatedResumeRepository.findByOriginalResumeIdOrderByVersionNumberAsc(resumeId).stream()
                .filter(g -> g.getUser().getEmail().equals(userEmail))
                .map(g -> mapToResponse(g, readSummary(g)))
                .toList();
    }

    public com.cvanalyzer.dto.PageResponse<TailoredResumeResponse> getUserTailoredResumes(
            String userEmail, int page, int size) {
        User user = getUser(userEmail);
        Page<GeneratedResume> p = generatedResumeRepository.findByUserOrderByCreatedAtDesc(
                user, PageRequest.of(page, size));
        List<TailoredResumeResponse> content = p.getContent().stream()
                .map(g -> mapToResponse(g, readSummary(g)))
                .toList();
        return com.cvanalyzer.dto.PageResponse.<TailoredResumeResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }

    public TailoredResumeResponse getById(Long id, String userEmail) {
        GeneratedResume g = getOwned(id, userEmail);
        return mapToResponse(g, readSummary(g));
    }

    /** Comparison data: original vs optimized + structured change summary (Feature 8). */
    public TailoredResumeResponse getComparison(Long id, String userEmail) {
        return getById(id, userEmail);
    }

    public byte[] downloadPdf(Long id, String userEmail) {
        GeneratedResume g = getOwned(id, userEmail);
        byte[] bytes;
        if (g.getPdfPath() != null) {
            bytes = fileStorageService.loadFileAsBytes(g.getPdfPath());
        } else {
            // Fallback: regenerate on the fly if the stored file is missing.
            bytes = formattedPdfGenerator.generate(g.getOptimizedContent(), PdfStyleProfile.defaults());
        }
        auditService.record(g.getUser(), AuditLog.AuditAction.GENERATED_RESUME_DOWNLOAD,
                g.getOriginalResume().getId(), g.getAiProvider(),
                "Downloaded tailored resume v" + g.getVersionNumber());
        return bytes;
    }

    // ── internals ───────────────────────────────────────────────────────────

    private AiRequest buildGenerationRequest(String originalText, String jobDescription,
                                             ResumeGroundingValidator.ValidationOutcome previous) {
        StringBuilder system = new StringBuilder();
        system.append("You are an expert resume optimizer and ATS specialist. ")
              .append("You tailor an existing resume to a specific job description.\n\n")
              .append("CRITICAL RULES — you must NEVER invent information:\n")
              .append("- Use ONLY information already present in the ORIGINAL RESUME.\n")
              .append("- ALLOWED: reordering sections, rewriting/rephrasing descriptions, improving wording, ")
              .append("improving ATS keyword matching using terms already in the resume, prioritizing relevant ")
              .append("experience, highlighting relevant projects.\n")
              .append("- NOT ALLOWED: adding skills, certifications, projects, experience, employers, job titles, ")
              .append("dates, metrics or achievements that are not in the original resume.\n")
              .append("- Do NOT introduce keywords from the job description that the candidate does not already have.\n");

        if (previous != null && !previous.isGrounded()) {
            system.append("\nYour previous attempt INTRODUCED UNVERIFIABLE CONTENT and was rejected: ")
                  .append(previous.getIssues())
                  .append(". Regenerate WITHOUT any of these and without any other invented facts.\n");
        }

        String user = "Return ONLY this JSON object:\n" +
                "{\n" +
                "  \"optimizedResume\": \"the full optimized resume as plain text with line breaks\",\n" +
                "  \"reorderedSections\": [\"section names moved\"],\n" +
                "  \"rewrittenSections\": [\"sections/bullets rephrased\"],\n" +
                "  \"keywordsEmphasized\": [\"keywords surfaced from the resume\"],\n" +
                "  \"atsImprovements\": [\"ATS improvements applied\"]\n" +
                "}\n\n" +
                "ORIGINAL RESUME:\n" + originalText + "\n\nJOB DESCRIPTION:\n" + jobDescription;

        return AiRequest.builder()
                .message(AiMessage.system(system.toString()))
                .message(AiMessage.user(user))
                .temperature(0.3)
                .maxTokens(3000)
                .jsonMode(true)
                .build();
    }

    private String renderAndStorePdf(Resume resume, String optimizedContent) {
        PdfStyleProfile profile = PdfStyleProfile.defaults();
        try {
            if (isPdf(resume)) {
                byte[] originalBytes = fileStorageService.loadFileAsBytes(resume.getFilePath());
                profile = pdfStyleAnalyzer.analyze(originalBytes);
            }
        } catch (Exception e) {
            log.warn("Could not analyze original PDF style; using defaults: {}", e.getMessage());
        }
        byte[] pdf = formattedPdfGenerator.generate(optimizedContent, profile);
        String stored = fileStorageService.storeBytes(pdf, ".pdf");
        return fileStorageService.getFilePath(stored);
    }

    private boolean isPdf(Resume resume) {
        String type = resume.getFileType();
        String name = resume.getOriginalFileName();
        return ("application/pdf".equalsIgnoreCase(type))
                || (name != null && name.toLowerCase().endsWith(".pdf"));
    }

    private TailoredResumeResponse mapToResponse(GeneratedResume g, ChangeSummary summary) {
        return TailoredResumeResponse.builder()
                .id(g.getId())
                .originalResumeId(g.getOriginalResume().getId())
                .originalFileName(g.getOriginalResume().getOriginalFileName())
                .versionNumber(g.getVersionNumber())
                .jobDescription(g.getJobDescription())
                .originalContent(g.getOriginalContent())
                .optimizedContent(g.getOptimizedContent())
                .changeSummary(summary)
                .aiProvider(g.getAiProvider())
                .validated(g.isValidated())
                .hasPdf(g.getPdfPath() != null)
                .createdAt(g.getCreatedAt())
                .build();
    }

    private GeneratedResume getOwned(Long id, String userEmail) {
        User user = getUser(userEmail);
        return generatedResumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Generated resume not found with id: " + id));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private List<String> safe(List<String> list) {
        return list == null ? new java.util.ArrayList<>() : list;
    }

    private String writeJson(ChangeSummary summary) {
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (Exception e) {
            return "{}";
        }
    }

    private ChangeSummary readSummary(GeneratedResume g) {
        if (g.getChangeSummary() == null || g.getChangeSummary().isBlank()) {
            return new ChangeSummary();
        }
        try {
            return objectMapper.readValue(g.getChangeSummary(), ChangeSummary.class);
        } catch (Exception e) {
            return new ChangeSummary();
        }
    }
}

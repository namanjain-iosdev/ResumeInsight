package com.cvanalyzer.service;

import com.cvanalyzer.dto.ImprovedResumeResponse;
import com.cvanalyzer.entity.Analysis;
import com.cvanalyzer.entity.ImprovedResume;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.exception.BadRequestException;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.repository.AnalysisRepository;
import com.cvanalyzer.repository.ImprovedResumeRepository;
import com.cvanalyzer.repository.UserRepository;
import com.cvanalyzer.service.ai.AIProvider;
import com.cvanalyzer.service.ai.AIProviderRouter;
import com.cvanalyzer.service.pdf.FormattedPdfGenerator;
import com.cvanalyzer.service.pdf.PdfStyleAnalyzer;
import com.cvanalyzer.service.pdf.PdfStyleProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImprovedResumeService {

    private final ImprovedResumeRepository improvedResumeRepository;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;
    private final AIProviderRouter aiProviderRouter;
    private final FileStorageService fileStorageService;
    private final PdfStyleAnalyzer pdfStyleAnalyzer;
    private final FormattedPdfGenerator formattedPdfGenerator;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Transactional
    public ImprovedResumeResponse generateImprovedResume(Long analysisId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Analysis analysis = analysisRepository.findByIdAndUser(analysisId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + analysisId));

        String resumeText = analysis.getResume().getExtractedText();
        if (resumeText == null || resumeText.isBlank()) {
            throw new BadRequestException("No resume text available for improvement");
        }

        AIProvider provider = aiProviderRouter.getProvider();
        String improvedContent = provider.improveResume(resumeText);

        ImprovedResume improved = improvedResumeRepository.findByAnalysisId(analysisId)
                .orElse(ImprovedResume.builder().analysis(analysis).build());
        improved.setOriginalContent(resumeText);
        improved.setImprovedContent(improvedContent);
        improved = improvedResumeRepository.save(improved);

        return mapToResponse(improved);
    }

    public ImprovedResumeResponse getImprovedResume(Long analysisId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Analysis analysis = analysisRepository.findByIdAndUser(analysisId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + analysisId));

        ImprovedResume improved = improvedResumeRepository.findByAnalysisId(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Improved resume not found for analysis: " + analysisId));

        return mapToResponse(improved);
    }

    public byte[] downloadImprovedResumePdf(Long analysisId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Analysis analysis = analysisRepository.findByIdAndUser(analysisId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));

        ImprovedResume improved = improvedResumeRepository.findByAnalysisId(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Improved resume not found"));

        // Preserve the original resume's visual structure where possible (Feature 4).
        PdfStyleProfile profile = PdfStyleProfile.defaults();
        try {
            com.cvanalyzer.entity.Resume resume = analysis.getResume();
            String name = resume.getOriginalFileName();
            boolean isPdf = "application/pdf".equalsIgnoreCase(resume.getFileType())
                    || (name != null && name.toLowerCase().endsWith(".pdf"));
            if (isPdf) {
                profile = pdfStyleAnalyzer.analyze(fileStorageService.loadFileAsBytes(resume.getFilePath()));
            }
        } catch (Exception e) {
            log.warn("Could not analyze original PDF style; using defaults: {}", e.getMessage());
        }
        return formattedPdfGenerator.generate(improved.getImprovedContent(), profile);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ImprovedResumeResponse mapToResponse(ImprovedResume improved) {
        return ImprovedResumeResponse.builder()
                .id(improved.getId())
                .analysisId(improved.getAnalysis().getId())
                .originalContent(improved.getOriginalContent())
                .improvedContent(improved.getImprovedContent())
                .createdAt(improved.getCreatedAt())
                .updatedAt(improved.getUpdatedAt())
                .build();
    }
}

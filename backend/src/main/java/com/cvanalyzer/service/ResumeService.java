package com.cvanalyzer.service;

import com.cvanalyzer.dto.PageResponse;
import com.cvanalyzer.dto.ResumeResponse;
import com.cvanalyzer.entity.Resume;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.exception.BadRequestException;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.exception.UnauthorizedException;
import com.cvanalyzer.repository.AnalysisRepository;
import com.cvanalyzer.repository.ResumeRepository;
import com.cvanalyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AnalysisRepository analysisRepository;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/msword",
            "text/plain"
    );

    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, String userEmail) {
        validateFile(file);
        User user = getUserByEmail(userEmail);

        String extractedText = fileStorageService.extractText(file);
        String checksum = computeChecksum(file);
        String storedFileName = fileStorageService.storeFile(file);
        String filePath = fileStorageService.getFilePath(storedFileName);

        // Preserve every upload as a new version (1, 2, 3 ...) for this user.
        int versionNumber = (int) resumeRepository.countByUser(user) + 1;

        Resume resume = Resume.builder()
                .user(user)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .filePath(filePath)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .versionNumber(versionNumber)
                .checksum(checksum)
                .extractedText(extractedText)
                .build();

        resume = resumeRepository.save(resume);
        auditService.record(user, com.cvanalyzer.entity.AuditLog.AuditAction.RESUME_UPLOAD,
                resume.getId(), null,
                "Uploaded '" + resume.getOriginalFileName() + "' (v" + versionNumber + ")");
        log.info("Resume uploaded: {} (v{}) by user: {}", resume.getId(), versionNumber, userEmail);
        return mapToResponse(resume, false);
    }

    public PageResponse<ResumeResponse> getUserResumes(String userEmail, int page, int size) {
        User user = getUserByEmail(userEmail);
        Page<Resume> resumePage = resumeRepository.findByUserOrderByUploadedAtDesc(user, PageRequest.of(page, size));
        List<ResumeResponse> content = resumePage.getContent().stream()
                .map(r -> {
                    boolean hasAnalysis = !analysisRepository.findByResumeId(r.getId()).isEmpty();
                    return mapToResponse(r, hasAnalysis);
                })
                .toList();
        return PageResponse.<ResumeResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .last(resumePage.isLast())
                .build();
    }

    /** Full version history for the user (every preserved upload, oldest first). */
    public List<ResumeResponse> getResumeVersions(String userEmail) {
        User user = getUserByEmail(userEmail);
        return resumeRepository.findByUserOrderByVersionNumberAsc(user).stream()
                .map(r -> mapToResponse(r, !analysisRepository.findByResumeId(r.getId()).isEmpty()))
                .toList();
    }

    public Resume getResumeById(Long resumeId, String userEmail) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found with id: " + resumeId));
        if (!resume.getUser().getEmail().equals(userEmail)) {
            throw new UnauthorizedException("You don't have access to this resume");
        }
        return resume;
    }

    @Transactional
    public void deleteResume(Long resumeId, String userEmail) {
        Resume resume = getResumeById(resumeId, userEmail);
        fileStorageService.deleteFile(resume.getFilePath());
        resumeRepository.delete(resume);
        log.info("Resume deleted: {} by user: {}", resumeId, userEmail);
    }

    public byte[] downloadResume(Long resumeId, String userEmail) {
        Resume resume = getResumeById(resumeId, userEmail);
        auditService.record(resume.getUser(), com.cvanalyzer.entity.AuditLog.AuditAction.RESUME_DOWNLOAD,
                resumeId, null, "Downloaded original '" + resume.getOriginalFileName() + "'");
        return fileStorageService.loadFileAsBytes(resume.getFilePath());
    }

    private String computeChecksum(MultipartFile file) {
        try {
            return fileStorageService.computeChecksum(file.getBytes());
        } catch (java.io.IOException e) {
            log.warn("Could not compute checksum for {}: {}", file.getOriginalFilename(), e.getMessage());
            return null;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("File type not allowed. Supported types: PDF, DOCX, DOC, TXT");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds 10MB limit");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ResumeResponse mapToResponse(Resume resume, boolean hasAnalysis) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .fileType(resume.getFileType())
                .fileSize(resume.getFileSize())
                .versionNumber(resume.getVersionNumber())
                .checksum(resume.getChecksum())
                .uploadedAt(resume.getUploadedAt())
                .hasAnalysis(hasAnalysis)
                .build();
    }
}

package com.cvanalyzer.service;

import com.cvanalyzer.dto.AuditHistoryResponse;
import com.cvanalyzer.dto.AuditLogResponse;
import com.cvanalyzer.dto.PageResponse;
import com.cvanalyzer.entity.*;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Records audit events and assembles the user-facing audit / version history
 * (Feature 2). History is derived from the canonical Resume / Analysis /
 * GeneratedResume tables, with an append-only {@link AuditLog} for raw events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ResumeRepository resumeRepository;
    private final AnalysisRepository analysisRepository;
    private final GeneratedResumeRepository generatedResumeRepository;
    private final UserRepository userRepository;

    /** Record an audit event. Never throws into the calling business flow. */
    @Transactional
    public void record(User user, AuditLog.AuditAction action, Long resumeId,
                       String aiProvider, String details) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .action(action)
                    .resumeId(resumeId)
                    .aiProvider(aiProvider)
                    .details(details)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to write audit log ({}): {}", action, e.getMessage());
        }
    }

    /** Raw audit log events for the current user. */
    public PageResponse<AuditLogResponse> getAuditLogs(String userEmail, int page, int size) {
        User user = getUser(userEmail);
        Page<AuditLog> logs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(), PageRequest.of(page, size));
        List<AuditLogResponse> content = logs.getContent().stream()
                .map(l -> AuditLogResponse.builder()
                        .id(l.getId())
                        .action(l.getAction().name())
                        .resumeId(l.getResumeId())
                        .aiProvider(l.getAiProvider())
                        .details(l.getDetails())
                        .createdAt(l.getCreatedAt())
                        .build())
                .toList();
        return PageResponse.<AuditLogResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(logs.getTotalElements())
                .totalPages(logs.getTotalPages())
                .last(logs.isLast())
                .build();
    }

    /** Combined audit history: each resume with its analyses + generated versions. */
    public PageResponse<AuditHistoryResponse> getAuditHistory(String userEmail, int page, int size) {
        User user = getUser(userEmail);
        Page<Resume> resumes = resumeRepository.findByUserOrderByUploadedAtDesc(
                user, PageRequest.of(page, size));

        List<AuditHistoryResponse> content = resumes.getContent().stream()
                .map(this::toHistoryRow)
                .toList();

        return PageResponse.<AuditHistoryResponse>builder()
                .content(content).page(page).size(size)
                .totalElements(resumes.getTotalElements())
                .totalPages(resumes.getTotalPages())
                .last(resumes.isLast())
                .build();
    }

    private AuditHistoryResponse toHistoryRow(Resume resume) {
        List<Analysis> analyses = analysisRepository.findByResumeId(resume.getId());
        Analysis latest = analyses.stream()
                .max(Comparator.comparing(Analysis::getCreatedAt))
                .orElse(null);

        List<AuditHistoryResponse.GeneratedVersion> generated =
                generatedResumeRepository.findByOriginalResumeIdOrderByVersionNumberAsc(resume.getId())
                        .stream()
                        .map(g -> AuditHistoryResponse.GeneratedVersion.builder()
                                .id(g.getId())
                                .versionNumber(g.getVersionNumber())
                                .aiProvider(g.getAiProvider())
                                .validated(g.isValidated())
                                .hasPdf(g.getPdfPath() != null)
                                .createdAt(g.getCreatedAt())
                                .build())
                        .toList();

        return AuditHistoryResponse.builder()
                .resumeId(resume.getId())
                .originalFileName(resume.getOriginalFileName())
                .versionNumber(resume.getVersionNumber())
                .fileSize(resume.getFileSize())
                .checksum(resume.getChecksum())
                .uploadedAt(resume.getUploadedAt())
                .lastAnalysisAt(latest != null ? latest.getCreatedAt() : null)
                .analysisProvider(latest != null ? latest.getAiProvider() : null)
                .analysisCount(analyses.size())
                .generatedVersions(generated)
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

package com.cvanalyzer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Append-only audit trail of significant CV-analysis actions
 * (upload, analyze, generate tailored resume, download).
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_resume", columnList = "resume_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuditAction action;

    @Column(name = "resume_id")
    private Long resumeId;

    @Column
    private String aiProvider;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum AuditAction {
        RESUME_UPLOAD,
        RESUME_ANALYZE,
        RESUME_IMPROVE,
        TAILORED_RESUME_GENERATE,
        RESUME_DOWNLOAD,
        GENERATED_RESUME_DOWNLOAD
    }
}

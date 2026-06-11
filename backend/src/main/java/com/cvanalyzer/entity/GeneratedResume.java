package com.cvanalyzer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A job-description-tailored resume generated from an original {@link Resume}.
 * Each generation creates a new version linked to its source resume so users
 * keep a full history (V1, V2, V3 ...) and can re-download any of them.
 */
@Entity
@Table(name = "generated_resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedResume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_resume_id", nullable = false)
    private Resume originalResume;

    /** Per-original-resume sequence number (1, 2, 3 ...). */
    @Column(nullable = false)
    private Integer versionNumber;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Column(columnDefinition = "LONGTEXT")
    private String originalContent;

    @Column(columnDefinition = "LONGTEXT")
    private String optimizedContent;

    /** JSON change summary used by the comparison view (reordered/rewritten/keywords). */
    @Column(columnDefinition = "LONGTEXT")
    private String changeSummary;

    /** Stored generated-PDF path for later re-download. */
    @Column
    private String pdfPath;

    @Column
    private String aiProvider;

    /** Whether grounding validation passed (no hallucinated content detected). */
    @Column
    @Builder.Default
    private boolean validated = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

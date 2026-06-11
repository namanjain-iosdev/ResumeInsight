package com.cvanalyzer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String fileType;

    private Long fileSize;

    /** Per-user upload sequence number (1, 2, 3 ...) for version history. */
    @Column
    private Integer versionNumber;

    /** SHA-256 hex checksum of the original uploaded bytes. */
    @Column(length = 64)
    private String checksum;

    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}

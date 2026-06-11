package com.cvanalyzer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "improved_resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImprovedResume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @Column(columnDefinition = "LONGTEXT")
    private String originalContent;

    @Column(columnDefinition = "LONGTEXT")
    private String improvedContent;

    @Column
    private String pdfPath;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

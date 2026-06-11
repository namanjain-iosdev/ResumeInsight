package com.cvanalyzer.controller;

import com.cvanalyzer.dto.ApiResponse;
import com.cvanalyzer.dto.ImprovedResumeResponse;
import com.cvanalyzer.service.ImprovedResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/improved-resumes")
@RequiredArgsConstructor
@Tag(name = "Improved Resumes", description = "AI resume improvement endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ImprovedResumeController {

    private final ImprovedResumeService improvedResumeService;

    @PostMapping("/generate/{analysisId}")
    @Operation(summary = "Generate an AI-improved version of a resume")
    public ResponseEntity<ApiResponse<ImprovedResumeResponse>> generate(
            @PathVariable Long analysisId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ImprovedResumeResponse response = improvedResumeService.generateImprovedResume(analysisId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Improved resume generated", response));
    }

    @GetMapping("/{analysisId}")
    @Operation(summary = "Get the improved resume for an analysis")
    public ResponseEntity<ApiResponse<ImprovedResumeResponse>> getImprovedResume(
            @PathVariable Long analysisId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ImprovedResumeResponse response = improvedResumeService.getImprovedResume(analysisId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Improved resume retrieved", response));
    }

    @GetMapping("/{analysisId}/download-pdf")
    @Operation(summary = "Download improved resume as PDF")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long analysisId,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] pdfBytes = improvedResumeService.downloadImprovedResumePdf(analysisId, userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"improved-resume.pdf\"")
                .body(pdfBytes);
    }
}

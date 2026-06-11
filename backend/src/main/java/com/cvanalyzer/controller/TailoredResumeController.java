package com.cvanalyzer.controller;

import com.cvanalyzer.dto.ApiResponse;
import com.cvanalyzer.dto.GenerateTailoredRequest;
import com.cvanalyzer.dto.PageResponse;
import com.cvanalyzer.dto.TailoredResumeResponse;
import com.cvanalyzer.service.TailoredResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tailored-resumes")
@RequiredArgsConstructor
@Tag(name = "Tailored Resumes", description = "Job-description-based resume optimization")
@SecurityRequirement(name = "bearerAuth")
public class TailoredResumeController {

    private final TailoredResumeService tailoredResumeService;

    @PostMapping("/generate")
    @Operation(summary = "Generate a job-tailored, grounded resume from an existing resume")
    public ResponseEntity<ApiResponse<TailoredResumeResponse>> generate(
            @Valid @RequestBody GenerateTailoredRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TailoredResumeResponse response = tailoredResumeService.generate(
                request.getResumeId(), request.getJobDescription(), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Tailored resume generated", response));
    }

    @GetMapping
    @Operation(summary = "List the current user's tailored resumes")
    public ResponseEntity<ApiResponse<PageResponse<TailoredResumeResponse>>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Tailored resumes retrieved",
                tailoredResumeService.getUserTailoredResumes(userDetails.getUsername(), page, size)));
    }

    @GetMapping("/by-resume/{resumeId}")
    @Operation(summary = "Version history of tailored resumes for an original resume")
    public ResponseEntity<ApiResponse<List<TailoredResumeResponse>>> byResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Versions retrieved",
                tailoredResumeService.getVersionsForResume(resumeId, userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a generated tailored resume")
    public ResponseEntity<ApiResponse<TailoredResumeResponse>> get(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Tailored resume retrieved",
                tailoredResumeService.getById(id, userDetails.getUsername())));
    }

    @GetMapping("/{id}/comparison")
    @Operation(summary = "Original vs optimized comparison with change summary")
    public ResponseEntity<ApiResponse<TailoredResumeResponse>> comparison(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Comparison retrieved",
                tailoredResumeService.getComparison(id, userDetails.getUsername())));
    }

    @GetMapping("/{id}/download-pdf")
    @Operation(summary = "Download the generated tailored resume PDF")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] pdf = tailoredResumeService.downloadPdf(id, userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tailored-resume.pdf\"")
                .body(pdf);
    }
}

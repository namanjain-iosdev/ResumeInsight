package com.cvanalyzer.controller;

import com.cvanalyzer.dto.*;
import com.cvanalyzer.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "CV analysis endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze a resume with AI")
    public ResponseEntity<ApiResponse<AnalysisResponse>> analyzeResume(
            @Valid @RequestBody AnalyzeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AnalysisResponse response = analysisService.analyzeResume(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Analysis complete", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated list of user analyses")
    public ResponseEntity<ApiResponse<PageResponse<AnalysisResponse>>> getUserAnalyses(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<AnalysisResponse> response = analysisService.getUserAnalyses(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Analyses retrieved", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific analysis by ID")
    public ResponseEntity<ApiResponse<AnalysisResponse>> getAnalysis(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        AnalysisResponse response = analysisService.getAnalysisById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Analysis retrieved", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an analysis")
    public ResponseEntity<ApiResponse<Void>> deleteAnalysis(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        analysisService.deleteAnalysis(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Analysis deleted successfully"));
    }
}

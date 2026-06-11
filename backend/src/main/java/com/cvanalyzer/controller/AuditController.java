package com.cvanalyzer.controller;

import com.cvanalyzer.dto.*;
import com.cvanalyzer.service.AuditService;
import com.cvanalyzer.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit trail & version history")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditService auditService;
    private final ResumeService resumeService;

    @GetMapping("/history")
    @Operation(summary = "Combined audit history (uploads + analyses + generated versions)")
    public ResponseEntity<ApiResponse<PageResponse<AuditHistoryResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Audit history retrieved",
                auditService.getAuditHistory(userDetails.getUsername(), page, size)));
    }

    @GetMapping("/logs")
    @Operation(summary = "Raw audit log events")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getLogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved",
                auditService.getAuditLogs(userDetails.getUsername(), page, size)));
    }

    @GetMapping("/resume-versions")
    @Operation(summary = "Full resume version history for the current user")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getResumeVersions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Resume versions retrieved",
                resumeService.getResumeVersions(userDetails.getUsername())));
    }
}

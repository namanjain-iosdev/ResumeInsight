package com.cvanalyzer.controller;

import com.cvanalyzer.dto.ApiResponse;
import com.cvanalyzer.dto.PageResponse;
import com.cvanalyzer.dto.ResumeResponse;
import com.cvanalyzer.service.ResumeService;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes", description = "Resume management")
@SecurityRequirement(name = "bearerAuth")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a resume file")
    public ResponseEntity<ApiResponse<ResumeResponse>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse response = resumeService.uploadResume(file, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get paginated list of user resumes")
    public ResponseEntity<ApiResponse<PageResponse<ResumeResponse>>> getUserResumes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<ResumeResponse> response = resumeService.getUserResumes(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Resumes retrieved", response));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a resume file")
    public ResponseEntity<byte[]> downloadResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] fileData = resumeService.downloadResume(id, userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume\"")
                .body(fileData);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        resumeService.deleteResume(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Resume deleted successfully"));
    }
}

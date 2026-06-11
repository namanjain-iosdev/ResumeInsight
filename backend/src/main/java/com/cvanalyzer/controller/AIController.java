package com.cvanalyzer.controller;

import com.cvanalyzer.dto.ApiResponse;
import com.cvanalyzer.service.ai.AIProviderRouter;
import com.cvanalyzer.service.ResumeService;
import com.cvanalyzer.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI provider management")
public class AIController {

    private final AIProviderRouter aiProviderRouter;

    @GetMapping("/status")
    @Operation(summary = "Check AI provider availability (public)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("activeProvider", aiProviderRouter.getActiveProviderName());
        status.put("available", aiProviderRouter.isAnyProviderAvailable());
        return ResponseEntity.ok(ApiResponse.success("AI status retrieved", status));
    }

    @GetMapping("/provider")
    @Operation(summary = "Get current AI provider name (requires auth)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Map<String, String>>> getProvider(
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> info = new HashMap<>();
        info.put("provider", aiProviderRouter.getActiveProviderName());
        return ResponseEntity.ok(ApiResponse.success("Provider info retrieved", info));
    }
}

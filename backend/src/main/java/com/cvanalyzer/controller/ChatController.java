package com.cvanalyzer.controller;

import com.cvanalyzer.dto.*;
import com.cvanalyzer.service.ChatService;
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
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI chat endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Send a message to the AI career coach")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ChatResponse response = chatService.chat(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Response received", response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get chat history")
    public ResponseEntity<ApiResponse<PageResponse<ChatResponse>>> getChatHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<ChatResponse> history = chatService.getChatHistory(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Chat history retrieved", history));
    }

    @DeleteMapping("/history")
    @Operation(summary = "Clear all chat history")
    public ResponseEntity<ApiResponse<Void>> clearHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        chatService.clearChatHistory(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Chat history cleared"));
    }
}

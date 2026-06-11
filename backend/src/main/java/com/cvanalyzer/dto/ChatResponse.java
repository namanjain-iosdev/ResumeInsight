package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatResponse {
    private Long id;
    private String message;
    private String response;
    private String provider;
    private String chatType;
    private LocalDateTime createdAt;
}

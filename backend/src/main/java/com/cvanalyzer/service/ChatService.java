package com.cvanalyzer.service;

import com.cvanalyzer.dto.ChatRequest;
import com.cvanalyzer.dto.ChatResponse;
import com.cvanalyzer.dto.PageResponse;
import com.cvanalyzer.entity.ChatHistory;
import com.cvanalyzer.entity.Resume;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.repository.ChatHistoryRepository;
import com.cvanalyzer.repository.ResumeRepository;
import com.cvanalyzer.repository.UserRepository;
import com.cvanalyzer.service.ai.AIProvider;
import com.cvanalyzer.service.ai.AIProviderRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final AIProviderRouter aiProviderRouter;

    @Transactional
    public ChatResponse chat(ChatRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        String resumeContext = null;
        if (request.getResumeId() != null) {
            Resume resume = resumeRepository.findById(request.getResumeId()).orElse(null);
            if (resume != null && resume.getUser().getEmail().equals(userEmail)) {
                resumeContext = resume.getExtractedText();
            }
        }

        AIProvider provider = aiProviderRouter.getProvider();
        String aiResponse = provider.chat(request.getMessage(), request.getChatType(), resumeContext);

        ChatHistory history = ChatHistory.builder()
                .user(user)
                .message(request.getMessage())
                .response(aiResponse)
                .provider(provider.getProviderName())
                .chatType(request.getChatType())
                .build();
        history = chatHistoryRepository.save(history);

        return mapToResponse(history);
    }

    public PageResponse<ChatResponse> getChatHistory(String userEmail, int page, int size) {
        User user = getUserByEmail(userEmail);
        Page<ChatHistory> chatPage = chatHistoryRepository.findByUserOrderByCreatedAtAsc(user, PageRequest.of(page, size));
        List<ChatResponse> content = chatPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return PageResponse.<ChatResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(chatPage.getTotalElements())
                .totalPages(chatPage.getTotalPages())
                .last(chatPage.isLast())
                .build();
    }

    @Transactional
    public void clearChatHistory(String userEmail) {
        User user = getUserByEmail(userEmail);
        chatHistoryRepository.deleteAllByUser(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ChatResponse mapToResponse(ChatHistory history) {
        return ChatResponse.builder()
                .id(history.getId())
                .message(history.getMessage())
                .response(history.getResponse())
                .provider(history.getProvider())
                .chatType(history.getChatType())
                .createdAt(history.getCreatedAt())
                .build();
    }
}

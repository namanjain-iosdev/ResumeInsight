package com.cvanalyzer.service;

import com.cvanalyzer.dto.AdminStatsResponse;
import com.cvanalyzer.dto.PageResponse;
import com.cvanalyzer.dto.UserResponse;
import com.cvanalyzer.entity.Role;
import com.cvanalyzer.entity.User;
import com.cvanalyzer.exception.ResourceNotFoundException;
import com.cvanalyzer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final AnalysisRepository analysisRepository;
    private final ChatHistoryRepository chatHistoryRepository;

    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalResumes = resumeRepository.count();
        long totalAnalyses = analysisRepository.count();
        long totalChats = chatHistoryRepository.count();

        double avgScore = 0.0;
        List<User> users = userRepository.findAll();
        if (!users.isEmpty()) {
            double sum = 0;
            int count = 0;
            for (User u : users) {
                Double avg = analysisRepository.findAverageAtsScoreByUser(u);
                if (avg != null) {
                    sum += avg;
                    count++;
                }
            }
            if (count > 0) avgScore = sum / count;
        }

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalResumes(totalResumes)
                .totalAnalyses(totalAnalyses)
                .totalChats(totalChats)
                .averageAtsScore(Math.round(avgScore * 100.0) / 100.0)
                .build();
    }

    public PageResponse<UserResponse> getAllUsers(String search, int page, int size) {
        Page<User> userPage;
        if (search != null && !search.isBlank()) {
            userPage = userRepository.searchUsers(search, PageRequest.of(page, size));
        } else {
            userPage = userRepository.findAll(PageRequest.of(page, size));
        }
        List<UserResponse> content = userPage.getContent().stream()
                .map(this::mapToUserResponse)
                .toList();
        return PageResponse.<UserResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .last(userPage.isLast())
                .build();
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse toggleUserEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setEnabled(!user.isEnabled());
        user = userRepository.save(user);
        log.info("User {} enabled status set to: {}", userId, user.isEnabled());
        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
        log.info("User deleted by admin: {}", userId);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .resumeCount(resumeRepository.countByUser(user))
                .analysisCount(analysisRepository.countByUser(user))
                .build();
    }
}

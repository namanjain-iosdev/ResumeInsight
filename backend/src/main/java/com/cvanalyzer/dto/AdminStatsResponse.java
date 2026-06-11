package com.cvanalyzer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalResumes;
    private long totalAnalyses;
    private long totalChats;
    private double averageAtsScore;
}

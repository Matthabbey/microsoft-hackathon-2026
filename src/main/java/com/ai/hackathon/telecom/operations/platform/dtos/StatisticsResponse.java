package com.ai.hackathon.telecom.operations.platform.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticsResponse {

    private UserStats activeUsers;
    private CallStats callsProcessed;
    private AiActionStats aiActions;
    private ResponseTimeStats averageResponseTime;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserStats {
        private long totalUsers;
        private long activeUsers;
        private long lockedUsers;
        private Map<String, Long> usersByRole;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CallStats {
        private long totalCalls;
        private long completedCalls;
        private long failedCalls;
        private long inProgressCalls;
        private long inboundCalls;
        private long outboundCalls;
        private BigDecimal totalCost;
        private Map<String, Long> callsByStatus;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AiActionStats {
        private long totalActions;
        private long successfulActions;
        private long failedActions;
        private Map<String, Long> actionsByType;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ResponseTimeStats {
        private Double averageDurationSeconds;
        private Integer maxDurationSeconds;
        private Integer minDurationSeconds;
        private long totalCallsWithDuration;
    }
}

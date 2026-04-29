package com.ai.hackathon.telecom.operations.platform.statistics;

import com.ai.hackathon.telecom.operations.platform.audit.AuditAction;
import com.ai.hackathon.telecom.operations.platform.call.CallStatus;
import com.ai.hackathon.telecom.operations.platform.dtos.StatisticsResponse;
import com.ai.hackathon.telecom.operations.platform.repository.AuditLogRepository;
import com.ai.hackathon.telecom.operations.platform.repository.CallRecordRepository;
import com.ai.hackathon.telecom.operations.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final UserRepository userRepository;
    private final CallRecordRepository callRecordRepository;
    private final AuditLogRepository auditLogRepository;

    public StatisticsResponse getOverviewStatistics() {
        return StatisticsResponse.builder()
                .activeUsers(buildUserStats())
                .callsProcessed(buildCallStats())
                .aiActions(buildAiActionStats())
                .averageResponseTime(buildResponseTimeStats())
                .build();
    }

    private StatisticsResponse.UserStats buildUserStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrueAndAccountLockedFalse();
        long lockedUsers = userRepository.countByAccountLockedTrue();

        Map<String, Long> usersByRole = new LinkedHashMap<>();
        List<Object[]> roleCounts = userRepository.countUsersByRole();
        for (Object[] row : roleCounts) {
            String roleName = (String) row[0];
            Long count = (Long) row[1];
            usersByRole.put(roleName, count);
        }

        return StatisticsResponse.UserStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .lockedUsers(lockedUsers)
                .usersByRole(usersByRole)
                .build();
    }

    private StatisticsResponse.CallStats buildCallStats() {
        long totalCalls = callRecordRepository.count();
        long completedCalls = callRecordRepository.countByStatus(CallStatus.COMPLETED);
        long failedCalls = callRecordRepository.countByStatus(CallStatus.FAILED);
        long inProgressCalls = callRecordRepository.countByStatus(CallStatus.IN_PROGRESS);
        long inboundCalls = callRecordRepository.countByDirection(
                com.ai.hackathon.telecom.operations.platform.call.CallDirection.INBOUND);
        long outboundCalls = callRecordRepository.countByDirection(
                com.ai.hackathon.telecom.operations.platform.call.CallDirection.OUTBOUND);
        BigDecimal totalCost = callRecordRepository.sumTotalCost();

        Map<String, Long> callsByStatus = new LinkedHashMap<>();
        List<Object[]> statusCounts = callRecordRepository.countByStatusGrouped();
        for (Object[] row : statusCounts) {
            CallStatus status = (CallStatus) row[0];
            Long count = (Long) row[1];
            callsByStatus.put(status.name(), count);
        }

        return StatisticsResponse.CallStats.builder()
                .totalCalls(totalCalls)
                .completedCalls(completedCalls)
                .failedCalls(failedCalls)
                .inProgressCalls(inProgressCalls)
                .inboundCalls(inboundCalls)
                .outboundCalls(outboundCalls)
                .totalCost(totalCost)
                .callsByStatus(callsByStatus)
                .build();
    }

    private StatisticsResponse.AiActionStats buildAiActionStats() {
        long totalActions = auditLogRepository.countNonHttpActions();
        long successfulActions = auditLogRepository.countSuccessfulNonHttpActions();
        long failedActions = auditLogRepository.countFailedNonHttpActions();

        Map<String, Long> actionsByType = new LinkedHashMap<>();
        List<Object[]> actionCounts = auditLogRepository.countByActionGrouped();
        for (Object[] row : actionCounts) {
            AuditAction action = (AuditAction) row[0];
            Long count = (Long) row[1];
            actionsByType.put(action.name(), count);
        }

        return StatisticsResponse.AiActionStats.builder()
                .totalActions(totalActions)
                .successfulActions(successfulActions)
                .failedActions(failedActions)
                .actionsByType(actionsByType)
                .build();
    }

    private StatisticsResponse.ResponseTimeStats buildResponseTimeStats() {
        Double avgDuration = callRecordRepository.averageDuration();
        Integer maxDuration = callRecordRepository.maxDuration();
        Integer minDuration = callRecordRepository.minDuration();
        long totalWithDuration = callRecordRepository.countByDurationNotNull();

        return StatisticsResponse.ResponseTimeStats.builder()
                .averageDurationSeconds(avgDuration)
                .maxDurationSeconds(maxDuration)
                .minDurationSeconds(minDuration)
                .totalCallsWithDuration(totalWithDuration)
                .build();
    }
}

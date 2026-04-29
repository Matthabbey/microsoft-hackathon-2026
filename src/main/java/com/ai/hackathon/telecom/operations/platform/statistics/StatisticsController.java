package com.ai.hackathon.telecom.operations.platform.statistics;

import com.ai.hackathon.telecom.operations.platform.dtos.ApiResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.StatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Statistics", description = "Platform overview and statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('VIEWER')")
    @Operation(summary = "Get platform overview statistics",
            description = "Returns active users, calls processed, AI actions, and average response time")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics() {
        StatisticsResponse statistics = statisticsService.getOverviewStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", statistics));
    }
}

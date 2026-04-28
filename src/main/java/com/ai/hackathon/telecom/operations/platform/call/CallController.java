package com.ai.hackathon.telecom.operations.platform.call;

import com.ai.hackathon.telecom.operations.platform.dtos.ApiResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.CallRecordResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.InitiateCallRequest;
import com.ai.hackathon.telecom.operations.platform.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
@Tag(name = "Calls", description = "Call tracking and management")
public class CallController {

    private final CallTrackingService callTrackingService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Get all calls (Manager+ only)")
    public ResponseEntity<ApiResponse<Page<CallRecordResponse>>> getAllCalls(Pageable pageable) {
        Page<CallRecordResponse> allCalls = callTrackingService.getAllCalls(pageable);
        return ResponseEntity.ok(ApiResponse.success("Calls retrieved successfully", allCalls));
    }

    @PostMapping
    @Operation(summary = "Initiate an outbound call")
    public ResponseEntity<ApiResponse<CallRecordResponse>> initiateCall(
            @Valid @RequestBody InitiateCallRequest request,
            @AuthenticationPrincipal User user
    ) {
        CallRecordResponse response = callTrackingService.initiateCall(request, user);
        return ResponseEntity.ok(ApiResponse.success("Call initiated successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get call history for the current user")
    public ResponseEntity<ApiResponse<Page<CallRecordResponse>>> getCallHistory(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        Page<CallRecordResponse> history = callTrackingService.getCallHistory(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Call history retrieved successfully", history));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter calls by criteria")
    public ResponseEntity<ApiResponse<Page<CallRecordResponse>>> filterCalls(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) CallStatus status,
            @RequestParam(required = false) CallDirection direction,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            Pageable pageable
    ) {
        Page<CallRecordResponse> results = callTrackingService.getCallsByFilters(
                user.getId(), status, direction, from, to, pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Calls filtered successfully", results));
    }

    @PostMapping("/logs/sync")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Sync call logs from Twilio into local database")
    public ResponseEntity<ApiResponse<List<CallRecordResponse>>> syncCallLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal User user
    ) {
        List<CallRecordResponse> synced = callTrackingService.syncCallLogsFromTwilio(from, to, user);
        return ResponseEntity.ok(ApiResponse.success("Call logs synced successfully", synced));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a call record by ID")
    public ResponseEntity<ApiResponse<CallRecordResponse>> getCallById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Call record retrieved successfully", callTrackingService.getCallById(id)));
    }

    @GetMapping("/sid/{callSid}")
    @Operation(summary = "Get a call record by Twilio CallSid")
    public ResponseEntity<ApiResponse<CallRecordResponse>> getCallBySid(@PathVariable String callSid) {
        return ResponseEntity.ok(ApiResponse.success("Call record retrieved successfully", callTrackingService.getCallBySid(callSid)));
    }
}

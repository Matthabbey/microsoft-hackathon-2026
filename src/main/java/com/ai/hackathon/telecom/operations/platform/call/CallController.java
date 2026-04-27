package com.ai.hackathon.telecom.operations.platform.call;

import com.ai.hackathon.telecom.operations.platform.dtos.CallRecordResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.InitiateCallRequest;
import com.ai.hackathon.telecom.operations.platform.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user/calls")
@RequiredArgsConstructor
@Tag(name = "Calls", description = "Call tracking and management")
public class CallController {

    private final CallTrackingService callTrackingService;

    @PostMapping
    @Operation(summary = "Initiate an outbound call")
    public ResponseEntity<CallRecordResponse> initiateCall(
            @Valid @RequestBody InitiateCallRequest request,
            @AuthenticationPrincipal User user
    ) {
        CallRecordResponse response = callTrackingService.initiateCall(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get call history for the current user")
    public ResponseEntity<Page<CallRecordResponse>> getCallHistory(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        Page<CallRecordResponse> history = callTrackingService.getCallHistory(user.getId(), pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/filter")
    @Operation(summary = "Filter calls by criteria")
    public ResponseEntity<Page<CallRecordResponse>> filterCalls(
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
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a call record by ID")
    public ResponseEntity<CallRecordResponse> getCallById(@PathVariable Long id) {
        return ResponseEntity.ok(callTrackingService.getCallById(id));
    }

    @GetMapping("/sid/{callSid}")
    @Operation(summary = "Get a call record by Twilio CallSid")
    public ResponseEntity<CallRecordResponse> getCallBySid(@PathVariable String callSid) {
        return ResponseEntity.ok(callTrackingService.getCallBySid(callSid));
    }
}

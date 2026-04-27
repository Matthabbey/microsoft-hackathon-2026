package com.ai.hackathon.telecom.operations.platform.call;

import com.ai.hackathon.telecom.operations.platform.audit.AuditAction;
import com.ai.hackathon.telecom.operations.platform.audit.AuditLog;
import com.ai.hackathon.telecom.operations.platform.audit.AuditResult;
import com.ai.hackathon.telecom.operations.platform.audit.AuditService;
import com.ai.hackathon.telecom.operations.platform.dtos.CallRecordResponse;
import com.ai.hackathon.telecom.operations.platform.dtos.CallStatusUpdate;
import com.ai.hackathon.telecom.operations.platform.dtos.InitiateCallRequest;
import com.ai.hackathon.telecom.operations.platform.repository.CallRecordRepository;
import com.ai.hackathon.telecom.operations.platform.user.User;
import com.twilio.rest.api.v2010.account.Call;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallTrackingService {

    private final TwilioService twilioService;
    private final CallRecordRepository callRecordRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuditService auditService;

    @Transactional
    public CallRecordResponse initiateCall(InitiateCallRequest request, User user) {
        Call call = twilioService.makeCall(request.getTo(), request.getFrom());

        CallRecord record = CallRecord.builder()
                .callSid(call.getSid())
                .fromNumber(call.getFrom())
                .toNumber(call.getTo())
                .direction(CallDirection.OUTBOUND)
                .status(CallStatus.INITIATED)
                .startTime(LocalDateTime.now())
                .user(user)
                .build();

        record = callRecordRepository.save(record);

        // Audit log
        auditService.logEventAsync(AuditLog.builder()
                .action(AuditAction.CALL_INITIATED)
                .result(AuditResult.SUCCESS)
                .userId(user.getId() != null ? user.getId().longValue() : null)
                .userEmail(user.getEmail())
                .entityType("CallRecord")
                .entityId(record.getCallSid())
                .timestamp(LocalDateTime.now())
                .description("Outbound call initiated to " + request.getTo())
        );

        // WebSocket broadcast
        CallStatusUpdate update = buildStatusUpdate(record, null);
        messagingTemplate.convertAndSend("/topic/calls", update);
        if (user.getEmail() != null) {
            messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/calls", update);
        }

        return toResponse(record);
    }

    @Transactional
    public void handleStatusCallback(Map<String, String> params) {
        String callSid = params.get("CallSid");
        String callStatus = params.get("CallStatus");
        String duration = params.get("CallDuration");
        String price = params.get("Price");
        String currency = params.get("PriceUnit");
        String recordingUrl = params.get("RecordingUrl");

        if (callSid == null) {
            log.warn("Received status callback without CallSid");
            return;
        }

        CallRecord record = callRecordRepository.findByCallSid(callSid).orElse(null);
        if (record == null) {
            log.warn("No call record found for CallSid: {}", callSid);
            return;
        }

        CallStatus previousStatus = record.getStatus();
        CallStatus newStatus = twilioService.mapTwilioStatus(callStatus);
        record.setStatus(newStatus);

        if (duration != null) {
            try {
                record.setDuration(Integer.parseInt(duration));
            } catch (NumberFormatException e) {
                log.warn("Invalid duration value: {}", duration);
            }
        }

        if (price != null) {
            try {
                record.setPrice(new BigDecimal(price));
            } catch (NumberFormatException e) {
                log.warn("Invalid price value: {}", price);
            }
        }

        if (currency != null) {
            record.setCurrency(currency);
        }

        if (recordingUrl != null) {
            record.setRecordingUrl(recordingUrl);
        }

        if (isTerminalStatus(newStatus)) {
            record.setEndTime(LocalDateTime.now());
        }

        record = callRecordRepository.save(record);

        // Audit log
        AuditAction auditAction = isTerminalStatus(newStatus)
                ? AuditAction.CALL_COMPLETED
                : AuditAction.CALL_STATUS_UPDATE;

        User user = record.getUser();
        auditService.logEventAsync(AuditLog.builder()
                .action(auditAction)
                .result(newStatus == CallStatus.FAILED ? AuditResult.FAILURE : AuditResult.SUCCESS)
                .userId(user != null && user.getId() != null ? user.getId().longValue() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .entityType("CallRecord")
                .entityId(callSid)
                .timestamp(LocalDateTime.now())
                .description("Call " + callSid + " status: " + previousStatus + " -> " + newStatus)
        );

        // WebSocket broadcast
        CallStatusUpdate update = buildStatusUpdate(record, previousStatus);
        messagingTemplate.convertAndSend("/topic/calls", update);
        if (user != null && user.getEmail() != null) {
            messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/calls", update);
        }
    }

    @Transactional
    public CallRecord handleInboundCall(Map<String, String> params) {
        String callSid = params.get("CallSid");
        String from = params.get("From");
        String to = params.get("To");

        CallRecord record = CallRecord.builder()
                .callSid(callSid)
                .fromNumber(from != null ? from : "unknown")
                .toNumber(to != null ? to : "unknown")
                .direction(CallDirection.INBOUND)
                .status(CallStatus.RINGING)
                .startTime(LocalDateTime.now())
                .build();

        record = callRecordRepository.save(record);

        // WebSocket broadcast
        CallStatusUpdate update = buildStatusUpdate(record, null);
        messagingTemplate.convertAndSend("/topic/calls", update);

        return record;
    }

    public Page<CallRecordResponse> getCallHistory(Integer userId, Pageable pageable) {
        return callRecordRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    public Page<CallRecordResponse> getCallsByFilters(Integer userId, CallStatus status,
                                                       CallDirection direction,
                                                       LocalDateTime from, LocalDateTime to,
                                                       Pageable pageable) {
        return callRecordRepository.findByFilters(userId, status, direction, from, to, pageable)
                .map(this::toResponse);
    }

    public CallRecordResponse getCallById(Long id) {
        CallRecord record = callRecordRepository.findById(id)
                .orElseThrow(() -> new CallNotFoundException("Call record not found with id: " + id));
        return toResponse(record);
    }

    public CallRecordResponse getCallBySid(String callSid) {
        CallRecord record = callRecordRepository.findByCallSid(callSid)
                .orElseThrow(() -> new CallNotFoundException("Call record not found with sid: " + callSid));
        return toResponse(record);
    }

    private boolean isTerminalStatus(CallStatus status) {
        return status == CallStatus.COMPLETED
                || status == CallStatus.FAILED
                || status == CallStatus.BUSY
                || status == CallStatus.NO_ANSWER
                || status == CallStatus.CANCELED;
    }

    private CallStatusUpdate buildStatusUpdate(CallRecord record, CallStatus previousStatus) {
        return CallStatusUpdate.builder()
                .callSid(record.getCallSid())
                .status(record.getStatus())
                .previousStatus(previousStatus)
                .direction(record.getDirection())
                .fromNumber(record.getFromNumber())
                .toNumber(record.getToNumber())
                .duration(record.getDuration())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private CallRecordResponse toResponse(CallRecord record) {
        return CallRecordResponse.builder()
                .id(record.getId())
                .callSid(record.getCallSid())
                .fromNumber(record.getFromNumber())
                .toNumber(record.getToNumber())
                .direction(record.getDirection())
                .status(record.getStatus())
                .duration(record.getDuration())
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .recordingUrl(record.getRecordingUrl())
                .price(record.getPrice())
                .currency(record.getCurrency())
                .userId(record.getUser() != null ? record.getUser().getId() : null)
                .userEmail(record.getUser() != null ? record.getUser().getEmail() : null)
                .build();
    }
}

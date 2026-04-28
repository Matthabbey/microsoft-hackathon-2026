package com.ai.hackathon.telecom.operations.platform.call;

import com.ai.hackathon.telecom.operations.platform.config.TwilioConfig;
import com.twilio.base.ResourceSet;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.security.RequestValidator;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioService {

    private final TwilioConfig twilioConfig;

    public Call makeCall(String to, String from) {
        String fromNumber = (from != null && !from.isBlank()) ? from : twilioConfig.getPhoneNumber();

        return Call.creator(
                        new PhoneNumber(to),
                        new PhoneNumber(fromNumber),
                        URI.create(twilioConfig.getVoiceUrl())
                )
                .setStatusCallback(URI.create(twilioConfig.getStatusCallbackUrl()))
                .setStatusCallbackEvent(java.util.List.of("initiated", "ringing", "answered", "completed"))
                .create();
    }

    public boolean validateRequest(String signature, String url, Map<String, String> params) {
        RequestValidator validator = new RequestValidator(twilioConfig.getAuthToken());
        return validator.validate(url, params, signature);
    }

    public List<Call> fetchCallLogs(LocalDate startDate, LocalDate endDate) {
        var reader = Call.reader();

        if (startDate != null) {
            reader.setStartTimeAfter(
                    ZonedDateTime.of(startDate.atStartOfDay(), ZoneOffset.UTC)
            );
        }
        if (endDate != null) {
            reader.setStartTimeBefore(
                    ZonedDateTime.of(endDate.plusDays(1).atStartOfDay(), ZoneOffset.UTC)
            );
        }

        ResourceSet<Call> calls = reader.read();
        return StreamSupport.stream(calls.spliterator(), false).toList();
    }

    public CallDirection mapTwilioDirection(String direction) {
        if (direction != null && direction.contains("inbound")) {
            return CallDirection.INBOUND;
        }
        return CallDirection.OUTBOUND;
    }

    public CallStatus mapTwilioStatus(String twilioStatus) {
        if (twilioStatus == null) {
            return CallStatus.INITIATED;
        }
        return switch (twilioStatus.toLowerCase()) {
            case "queued" -> CallStatus.QUEUED;
            case "ringing" -> CallStatus.RINGING;
            case "in-progress" -> CallStatus.IN_PROGRESS;
            case "completed" -> CallStatus.COMPLETED;
            case "failed" -> CallStatus.FAILED;
            case "busy" -> CallStatus.BUSY;
            case "no-answer" -> CallStatus.NO_ANSWER;
            case "canceled" -> CallStatus.CANCELED;
            default -> {
                log.warn("Unknown Twilio call status: {}", twilioStatus);
                yield CallStatus.INITIATED;
            }
        };
    }
}

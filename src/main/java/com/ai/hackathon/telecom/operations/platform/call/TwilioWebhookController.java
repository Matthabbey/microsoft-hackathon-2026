package com.ai.hackathon.telecom.operations.platform.call;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks/twilio")
@RequiredArgsConstructor
@Slf4j
@Hidden
public class TwilioWebhookController {

    private final CallTrackingService callTrackingService;
    private final TwilioService twilioService;

    @PostMapping(value = "/voice", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> handleInboundCall(
            @RequestParam Map<String, String> params,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
            HttpServletRequest request
    ) {
        if (!validateTwilioRequest(signature, request, params)) {
            log.warn("Invalid Twilio signature on voice webhook");
            return ResponseEntity.status(403).build();
        }

        log.info("Inbound call received: CallSid={}, From={}, To={}",
                params.get("CallSid"), params.get("From"), params.get("To"));

        callTrackingService.handleInboundCall(params);

        VoiceResponse response = new VoiceResponse.Builder()
                .say(new Say.Builder("Thank you for calling. Your call is being tracked.")
                        .voice(Say.Voice.ALICE)
                        .build())
                .build();

        return ResponseEntity.ok(response.toXml());
    }

    @PostMapping("/status")
    public ResponseEntity<Void> handleStatusCallback(
            @RequestParam Map<String, String> params,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
            HttpServletRequest request
    ) {
        if (!validateTwilioRequest(signature, request, params)) {
            log.warn("Invalid Twilio signature on status callback");
            return ResponseEntity.status(403).build();
        }

        log.info("Status callback: CallSid={}, Status={}",
                params.get("CallSid"), params.get("CallStatus"));

        callTrackingService.handleStatusCallback(params);

        return ResponseEntity.ok().build();
    }

    private boolean validateTwilioRequest(String signature, HttpServletRequest request,
                                           Map<String, String> params) {
        if (signature == null || signature.isBlank()) {
            log.warn("Missing X-Twilio-Signature header");
            return false;
        }

        String url = request.getRequestURL().toString();
        Map<String, String> paramsCopy = new HashMap<>(params);

        return twilioService.validateRequest(signature, url, paramsCopy);
    }
}

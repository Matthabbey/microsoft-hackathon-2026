package com.ai.hackathon.telecom.operations.platform.dtos;

import com.ai.hackathon.telecom.operations.platform.call.CallDirection;
import com.ai.hackathon.telecom.operations.platform.call.CallStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallRecordResponse {

    private Long id;
    private String callSid;
    private String fromNumber;
    private String toNumber;
    private CallDirection direction;
    private CallStatus status;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String recordingUrl;
    private BigDecimal price;
    private String currency;
    private Integer userId;
    private String userEmail;
}

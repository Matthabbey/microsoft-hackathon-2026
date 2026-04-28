package com.ai.hackathon.telecom.operations.platform.dtos;

import com.ai.hackathon.telecom.operations.platform.call.CallDirection;
import com.ai.hackathon.telecom.operations.platform.call.CallStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallStatusUpdate {

    private String callSid;
    private CallStatus status;
    private CallStatus previousStatus;
    private CallDirection direction;
    private String fromNumber;
    private String toNumber;
    private Integer duration;
    private LocalDateTime timestamp;
}

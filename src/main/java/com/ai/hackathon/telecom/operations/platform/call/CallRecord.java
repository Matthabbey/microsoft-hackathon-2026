package com.ai.hackathon.telecom.operations.platform.call;

import com.ai.hackathon.telecom.operations.platform.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_record", indexes = {
        @Index(name = "idx_call_sid", columnList = "callSid", unique = true),
        @Index(name = "idx_call_user_id", columnList = "user_id"),
        @Index(name = "idx_call_status", columnList = "status"),
        @Index(name = "idx_call_start_time", columnList = "startTime")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String callSid;

    @Column(nullable = false)
    private String fromNumber;

    @Column(nullable = false)
    private String toNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status;

    private Integer duration;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String recordingUrl;

    private BigDecimal price;

    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

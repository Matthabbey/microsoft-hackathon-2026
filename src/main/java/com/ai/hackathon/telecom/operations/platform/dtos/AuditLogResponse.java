package com.ai.hackathon.telecom.operations.platform.dtos;

import com.ai.hackathon.telecom.operations.platform.audit.AuditAction;
import com.ai.hackathon.telecom.operations.platform.audit.AuditResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userRoles;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String endpoint;
    private String httpMethod;
    private AuditResult result;
    private Integer httpStatus;
    private String description;
    private String details;
}

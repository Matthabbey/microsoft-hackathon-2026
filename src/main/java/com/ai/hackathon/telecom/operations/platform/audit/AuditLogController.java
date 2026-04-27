package com.ai.hackathon.telecom.operations.platform.audit;

import com.ai.hackathon.telecom.operations.platform.dtos.AuditLogResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Secured({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) AuditResult result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        // Log audit log access itself
        auditService.logEventAsync(AuditLog.builder()
                .action(AuditAction.AUDIT_LOG_ACCESS)
                .result(AuditResult.SUCCESS)
                .timestamp(LocalDateTime.now())
                .ipAddress(request.getRemoteAddr())
                .endpoint(request.getRequestURI())
                .httpMethod(request.getMethod())
                .userEmail(authentication.getName())
                .description("Admin accessed audit logs"));

        return ResponseEntity.ok(auditService.getAuditLogs(userId, action, entityType, result, from, to, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        auditService.logEventAsync(AuditLog.builder()
                .action(AuditAction.AUDIT_LOG_ACCESS)
                .result(AuditResult.SUCCESS)
                .timestamp(LocalDateTime.now())
                .ipAddress(request.getRemoteAddr())
                .endpoint(request.getRequestURI())
                .httpMethod(request.getMethod())
                .userEmail(authentication.getName())
                .description("Admin accessed audit logs for user " + userId));

        return ResponseEntity.ok(auditService.getAuditLogsByUserId(userId, pageable));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogsByAction(
            @PathVariable AuditAction action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        auditService.logEventAsync(AuditLog.builder()
                .action(AuditAction.AUDIT_LOG_ACCESS)
                .result(AuditResult.SUCCESS)
                .timestamp(LocalDateTime.now())
                .ipAddress(request.getRemoteAddr())
                .endpoint(request.getRequestURI())
                .httpMethod(request.getMethod())
                .userEmail(authentication.getName())
                .description("Admin accessed audit logs for action " + action));

        return ResponseEntity.ok(auditService.getAuditLogsByAction(action, pageable));
    }
}

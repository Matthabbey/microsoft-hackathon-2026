package com.ai.hackathon.telecom.operations.platform.audit;

import com.ai.hackathon.telecom.operations.platform.dtos.AuditLogResponse;
import com.ai.hackathon.telecom.operations.platform.repository.AuditLogRepository;
import com.ai.hackathon.telecom.operations.platform.user.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void logEventAsync(AuditLog.Builder builder) {
        try {
            auditLogRepository.save(builder.build());
        } catch (Exception e) {
            log.error("Failed to save audit log entry: {}", e.getMessage(), e);
        }
    }

    public void logEvent(AuditLog.Builder builder) {
        try {
            auditLogRepository.save(builder.build());
        } catch (Exception e) {
            log.error("Failed to save audit log entry: {}", e.getMessage(), e);
        }
    }

    /**
     * Logs an auth event. Extracts request data synchronously on the caller's thread,
     * then delegates the DB save asynchronously.
     */
    public void logAuthEvent(AuditAction action, AuditResult result, User user,
                             HttpServletRequest request, String description) {
        try {
            AuditLog.Builder builder = AuditLog.builder()
                    .action(action)
                    .result(result)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIp(request))
                    .endpoint(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .description(description);

            if (user != null) {
                builder.userId(user.getId() != null ? user.getId().longValue() : null)
                        .userEmail(user.getEmail())
                        .userRoles(user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(",")))
                        .entityType("User")
                        .entityId(user.getId() != null ? user.getId().toString() : null);
            }

            logEventAsync(builder);
        } catch (Exception e) {
            log.error("Failed to prepare auth audit log entry: {}", e.getMessage(), e);
        }
    }

    public void logAuthEventByEmail(AuditAction action, AuditResult result, String email,
                                    HttpServletRequest request, String description) {
        try {
            AuditLog.Builder builder = AuditLog.builder()
                    .action(action)
                    .result(result)
                    .userEmail(email)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIp(request))
                    .endpoint(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .entityType("User")
                    .description(description);

            logEventAsync(builder);
        } catch (Exception e) {
            log.error("Failed to prepare auth audit log entry: {}", e.getMessage(), e);
        }
    }

    public void logHttpRequest(HttpServletRequest request, int status) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            AuditLog.Builder builder = AuditLog.builder()
                    .action(AuditAction.HTTP_REQUEST)
                    .result(status >= 400 ? AuditResult.FAILURE : AuditResult.SUCCESS)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIp(request))
                    .endpoint(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .httpStatus(status);

            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
                builder.userId(user.getId() != null ? user.getId().longValue() : null)
                        .userEmail(user.getEmail())
                        .userRoles(user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(",")));
            }

            logEventAsync(builder);
        } catch (Exception e) {
            log.error("Failed to prepare HTTP audit log entry: {}", e.getMessage(), e);
        }
    }

    public void logSystemError(HttpServletRequest request, Exception ex) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            AuditLog.Builder builder = AuditLog.builder()
                    .action(AuditAction.SYSTEM_ERROR)
                    .result(AuditResult.FAILURE)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIp(request))
                    .endpoint(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .description(ex.getClass().getSimpleName() + ": " + ex.getMessage());

            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
                builder.userId(user.getId() != null ? user.getId().longValue() : null)
                        .userEmail(user.getEmail())
                        .userRoles(user.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(",")));
            }

            logEventAsync(builder);
        } catch (Exception e) {
            log.error("Failed to prepare system error audit log entry: {}", e.getMessage(), e);
        }
    }

    // Query methods

    public Page<AuditLogResponse> getAuditLogs(Long userId, AuditAction action, String entityType,
                                                AuditResult result, LocalDateTime from,
                                                LocalDateTime to, Pageable pageable) {
        return auditLogRepository.findByFilters(userId, action, entityType, result, from, to, pageable)
                .map(this::toResponse);
    }

    public Page<AuditLogResponse> getAuditLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    public Page<AuditLogResponse> getAuditLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable)
                .map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUserId())
                .userEmail(auditLog.getUserEmail())
                .userRoles(auditLog.getUserRoles())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .timestamp(auditLog.getTimestamp())
                .ipAddress(auditLog.getIpAddress())
                .endpoint(auditLog.getEndpoint())
                .httpMethod(auditLog.getHttpMethod())
                .result(auditLog.getResult())
                .httpStatus(auditLog.getHttpStatus())
                .description(auditLog.getDescription())
                .details(auditLog.getDetails())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

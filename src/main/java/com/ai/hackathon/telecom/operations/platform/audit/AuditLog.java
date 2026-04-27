package com.ai.hackathon.telecom.operations.platform.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_user_id", columnList = "userId"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_entity_type", columnList = "entityType")
})
@Getter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(updatable = false)
    private Long userId;

    @Column(updatable = false)
    private String userEmail;

    @Column(updatable = false)
    private String userRoles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AuditAction action;

    @Column(updatable = false)
    private String entityType;

    @Column(updatable = false)
    private String entityId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(updatable = false)
    private String ipAddress;

    @Column(updatable = false)
    private String endpoint;

    @Column(updatable = false)
    private String httpMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AuditResult result;

    @Column(updatable = false)
    private Integer httpStatus;

    @Column(updatable = false, length = 1000)
    private String description;

    @Column(updatable = false, length = 2000)
    private String details;

    protected AuditLog() {
        // JPA requires a default constructor
    }

    private AuditLog(Builder builder) {
        this.userId = builder.userId;
        this.userEmail = builder.userEmail;
        this.userRoles = builder.userRoles;
        this.action = builder.action;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
        this.ipAddress = builder.ipAddress;
        this.endpoint = builder.endpoint;
        this.httpMethod = builder.httpMethod;
        this.result = builder.result;
        this.httpStatus = builder.httpStatus;
        this.description = builder.description;
        this.details = builder.details;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder userRoles(String userRoles) {
            this.userRoles = userRoles;
            return this;
        }

        public Builder action(AuditAction action) {
            this.action = action;
            return this;
        }

        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder result(AuditResult result) {
            this.result = result;
            return this;
        }

        public Builder httpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(this);
        }
    }
}

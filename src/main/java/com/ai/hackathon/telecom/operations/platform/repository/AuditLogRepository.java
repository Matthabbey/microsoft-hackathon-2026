package com.ai.hackathon.telecom.operations.platform.repository;

import com.ai.hackathon.telecom.operations.platform.audit.AuditAction;
import com.ai.hackathon.telecom.operations.platform.audit.AuditLog;
import com.ai.hackathon.telecom.operations.platform.audit.AuditResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE "
            + "(:userId IS NULL OR a.userId = :userId) "
            + "AND (:action IS NULL OR a.action = :action) "
            + "AND (:entityType IS NULL OR a.entityType = :entityType) "
            + "AND (:result IS NULL OR a.result = :result) "
            + "AND (CAST(:from AS timestamp) IS NULL OR a.timestamp >= :from) "
            + "AND (CAST(:to AS timestamp) IS NULL OR a.timestamp <= :to)")
    Page<AuditLog> findByFilters(
            @Param("userId") Long userId,
            @Param("action") AuditAction action,
            @Param("entityType") String entityType,
            @Param("result") AuditResult result,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action NOT IN ('HTTP_REQUEST')")
    long countNonHttpActions();

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action NOT IN ('HTTP_REQUEST') AND a.result = 'SUCCESS'")
    long countSuccessfulNonHttpActions();

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action NOT IN ('HTTP_REQUEST') AND a.result = 'FAILURE'")
    long countFailedNonHttpActions();

    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.action NOT IN ('HTTP_REQUEST') GROUP BY a.action")
    List<Object[]> countByActionGrouped();
}

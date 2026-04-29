package com.ai.hackathon.telecom.operations.platform.repository;

import com.ai.hackathon.telecom.operations.platform.call.CallDirection;
import com.ai.hackathon.telecom.operations.platform.call.CallRecord;
import com.ai.hackathon.telecom.operations.platform.call.CallStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CallRecordRepository extends JpaRepository<CallRecord, Long> {

    Optional<CallRecord> findByCallSid(String callSid);

    Page<CallRecord> findByUserId(Integer userId, Pageable pageable);

    @Query("SELECT c FROM CallRecord c WHERE " +
            "(:userId IS NULL OR c.user.id = :userId) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:direction IS NULL OR c.direction = :direction) AND " +
            "(:from IS NULL OR c.startTime >= :from) AND " +
            "(:to IS NULL OR c.startTime <= :to)")
    Page<CallRecord> findByFilters(
            @Param("userId") Integer userId,
            @Param("status") CallStatus status,
            @Param("direction") CallDirection direction,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    long countByStatus(CallStatus status);

    long countByDirection(CallDirection direction);

    @Query("SELECT c.status, COUNT(c) FROM CallRecord c GROUP BY c.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT COALESCE(SUM(c.price), 0) FROM CallRecord c WHERE c.price IS NOT NULL")
    BigDecimal sumTotalCost();

    @Query("SELECT AVG(c.duration) FROM CallRecord c WHERE c.duration IS NOT NULL AND c.duration > 0")
    Double averageDuration();

    @Query("SELECT MAX(c.duration) FROM CallRecord c WHERE c.duration IS NOT NULL")
    Integer maxDuration();

    @Query("SELECT MIN(c.duration) FROM CallRecord c WHERE c.duration IS NOT NULL AND c.duration > 0")
    Integer minDuration();

    long countByDurationNotNull();
}

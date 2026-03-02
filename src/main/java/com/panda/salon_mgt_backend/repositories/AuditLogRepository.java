package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop200ByOrderByCreatedAtDesc();

    List<AuditLog> findBySalonIdOrderByCreatedAtDesc(Long salonId);

    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    List<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            Instant from,
            Instant to
    );
}
package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
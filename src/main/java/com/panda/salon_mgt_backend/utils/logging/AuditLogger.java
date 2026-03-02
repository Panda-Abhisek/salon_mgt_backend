package com.panda.salon_mgt_backend.utils.logging;

import com.panda.salon_mgt_backend.models.AuditLog;
import com.panda.salon_mgt_backend.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogger {

    private final AuditLogRepository repo;

    public void log(
            String action,
            Long salonId,
            Authentication auth,
            String details
    ) {
        String actor = auth != null ? auth.getName() : "SYSTEM";

        // Console log
        log.warn(
                "AUDIT action={} actor={} salonId={} details={}",
                action,
                actor,
                salonId,
                details
        );
        try {
            // DB persistence
            repo.save(
                    AuditLog.builder()
                            .action(action)
                            .actor(actor)
                            .salonId(salonId)
                            .details(details)
                            .createdAt(Instant.now())
                            .build()
            );
        } catch (Exception e) {
            log.error("AUDIT_PERSIST_FAILED action={}", action, e);
        }

    }
}
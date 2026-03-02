package com.panda.salon_mgt_backend.utils.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditLogger {

    public void log(
            String action,
            Long salonId,
            Authentication auth,
            String details
    ) {
        String actor = auth != null ? auth.getName() : "SYSTEM";

        log.warn(
                "AUDIT action={} actor={} salonId={} details={}",
                action,
                actor,
                salonId,
                details
        );
    }
}
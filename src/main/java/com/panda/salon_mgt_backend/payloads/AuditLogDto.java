package com.panda.salon_mgt_backend.payloads;

import com.panda.salon_mgt_backend.models.AuditLog;

import java.time.Instant;

public record AuditLogDto(
        Long id,
        Long salonId,
        String actor,
        String action,
        String details,
        Instant createdAt
) {
    public static AuditLogDto from(AuditLog a) {
        return new AuditLogDto(
                a.getId(),
                a.getSalonId(),
                a.getActor(),
                a.getAction(),
                a.getDetails(),
                a.getCreatedAt()
        );
    }
}
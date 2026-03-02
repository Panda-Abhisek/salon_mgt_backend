package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.payloads.AuditLogDto;

import java.time.Instant;
import java.util.List;

public interface AuditLogService {
    List<AuditLogDto> recent();
    List<AuditLogDto> bySalon(Long salonId);
    List<AuditLogDto> byAction(String action);
    List<AuditLogDto> byRange(Instant from, Instant to);
}
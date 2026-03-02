package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.payloads.AuditLogDto;
import com.panda.salon_mgt_backend.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repo;

    @Override
    public List<AuditLogDto> recent() {
        return repo.findTop200ByOrderByCreatedAtDesc()
                .stream()
                .map(AuditLogDto::from)
                .toList();
    }

    @Override
    public List<AuditLogDto> bySalon(Long salonId) {
        return repo.findBySalonIdOrderByCreatedAtDesc(salonId)
                .stream()
                .map(AuditLogDto::from)
                .toList();
    }

    @Override
    public List<AuditLogDto> byAction(String action) {
        return repo.findByActionOrderByCreatedAtDesc(action)
                .stream()
                .map(AuditLogDto::from)
                .toList();
    }

    @Override
    public List<AuditLogDto> byRange(Instant from, Instant to) {
        return repo.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to)
                .stream()
                .map(AuditLogDto::from)
                .toList();
    }
}
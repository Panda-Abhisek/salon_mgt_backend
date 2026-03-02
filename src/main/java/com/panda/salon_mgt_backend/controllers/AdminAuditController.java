package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.AuditLogDto;
import com.panda.salon_mgt_backend.services.analytics.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audits")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminAuditController {

    private final AuditLogService service;

    @GetMapping("/recent")
    public List<AuditLogDto> recent() {
        return service.recent();
    }

    @GetMapping("/salon/{id}")
    public List<AuditLogDto> bySalon(@PathVariable Long id) {
        return service.bySalon(id);
    }

    @GetMapping("/action/{action}")
    public List<AuditLogDto> byAction(@PathVariable String action) {
        return service.byAction(action);
    }

    @GetMapping("/range")
    public List<AuditLogDto> byRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return service.byRange(from, to);
    }
}
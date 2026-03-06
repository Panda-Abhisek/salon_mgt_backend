package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.AuditLogDto;
import com.panda.salon_mgt_backend.services.analytics.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<AuditLogDto>> recent() {
        return ResponseEntity.ok(service.recent());
    }

    @GetMapping("/salon/{id}")
    public ResponseEntity<List<AuditLogDto>> bySalon(@PathVariable Long id) {
        return ResponseEntity.ok(service.bySalon(id));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogDto>> byAction(@PathVariable String action) {
        return ResponseEntity.ok(service.byAction(action));
    }

    @GetMapping("/range")
    public ResponseEntity<List<AuditLogDto>> byRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return ResponseEntity.ok(service.byRange(from, to));
    }
}
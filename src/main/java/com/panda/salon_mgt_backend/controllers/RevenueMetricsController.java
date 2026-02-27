package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.RevenueMetrics;
import com.panda.salon_mgt_backend.services.impl.RevenueMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics/revenue")
@RequiredArgsConstructor
public class RevenueMetricsController {

    private final RevenueMetricsService service;

    @GetMapping
    public ResponseEntity<RevenueMetrics> getRevenueMetrics() {
        return ResponseEntity.ok(service.calculate());
    }
}
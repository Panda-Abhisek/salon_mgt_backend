package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.BillingMetricsResponse;
import com.panda.salon_mgt_backend.services.analytics.BillingMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/billing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class BillingMetricsController {

    private final BillingMetricsService billingMetricsService;

    @GetMapping("/metrics")
    public BillingMetricsResponse getMetrics() {
        return billingMetricsService.getMetrics();
    }
}
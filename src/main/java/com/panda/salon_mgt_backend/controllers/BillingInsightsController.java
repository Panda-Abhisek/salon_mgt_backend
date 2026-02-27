package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.BillingInsightsDto;
import com.panda.salon_mgt_backend.services.BillingInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/billing-insights")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BillingInsightsController {

    private final BillingInsightsService insightsService;

    @GetMapping
    public ResponseEntity<BillingInsightsDto> get() {
        return ResponseEntity.ok(insightsService.getBillingHealth());
    }
}
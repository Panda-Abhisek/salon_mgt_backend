package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.BillingObservabilityDto;
import com.panda.salon_mgt_backend.services.impl.BillingObservabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/billing-observability")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminBillingObservabilityController {

    private final BillingObservabilityService observabilityService;

    @GetMapping
    public BillingObservabilityDto snapshot() {
        return observabilityService.snapshot();
    }
}
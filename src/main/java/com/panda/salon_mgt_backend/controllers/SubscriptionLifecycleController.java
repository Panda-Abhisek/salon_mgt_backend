package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.payloads.SubscriptionLifecycleResponse;
import com.panda.salon_mgt_backend.services.analytics.SubscriptionLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionLifecycleController {

    private final SubscriptionLifecycleService lifecycleService;

    @GetMapping("/lifecycle")
    public SubscriptionLifecycleResponse getLifecycle(Authentication auth) {
        return lifecycleService.getLifecycle(auth);
    }
}
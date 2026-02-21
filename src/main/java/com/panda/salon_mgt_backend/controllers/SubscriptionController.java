package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.payloads.SubscriptionResponse;
import com.panda.salon_mgt_backend.payloads.UpgradePlanRequest;
import com.panda.salon_mgt_backend.services.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // 🟢 Get current plan
    @GetMapping
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public SubscriptionResponse getCurrent(Authentication auth) {
        Subscription sub = subscriptionService.getCurrentSubscription(auth);
        return map(sub);
    }

    // 🚀 Upgrade plan
    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public SubscriptionResponse upgrade(
            @Valid @RequestBody UpgradePlanRequest request,
            Authentication auth
    ) {
        Subscription upgraded =
                subscriptionService.upgradePlan(auth, request.plan());

        return map(upgraded);
    }

    private SubscriptionResponse map(Subscription sub) {
        if (sub == null) return null;

        return new SubscriptionResponse(
                sub.getPlan().getType(),
                sub.getStatus(),
                sub.getStartDate(),
                sub.getEndDate()
        );
    }
}
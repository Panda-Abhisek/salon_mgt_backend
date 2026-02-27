package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.RecoverySeverity;
import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;
import com.panda.salon_mgt_backend.payloads.*;
import com.panda.salon_mgt_backend.services.BillingService;
import com.panda.salon_mgt_backend.services.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final BillingService billingService;

    // 🟢 Get current plan
    @GetMapping
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public ResponseEntity<SubscriptionResponse> getCurrent(Authentication auth) {
        Subscription sub = subscriptionService.getCurrentSubscription(auth);
        return ResponseEntity.ok(map(sub));
    }

    // 🚀 Upgrade plan
    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('SALON_ADMIN')")
    public ResponseEntity<BillingIntentResponse> upgrade(
            @Valid @RequestBody UpgradePlanRequest request,
            Authentication auth
    ) {
        BillingIntentResponse intent = subscriptionService.upgradePlan(auth, request.plan());
        return ResponseEntity.ok(intent);
    }

    @PreAuthorize("hasRole('SALON_ADMIN')")
    @PostMapping("/start-trial")
    public ResponseEntity<SubscriptionResponse> startTrial(Authentication auth) {
        Subscription sub = subscriptionService.startTrial(auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(map(sub));
    }

    private SubscriptionResponse map(Subscription sub) {
        if (sub == null) return null;
        Plan plan = sub.getPlan();
        PlanLimitsDto limits = new PlanLimitsDto(
                plan.getMaxStaff(),
                plan.getMaxServices(),
                plan.getMaxBookings()
        );

        PlanFeaturesDto features = new PlanFeaturesDto(
                plan.getAnalyticsEnabled(),
                plan.getSmartAlertsEnabled()
        );

        int retries = sub.getRetryCount() == null ? 0 : sub.getRetryCount();
        boolean delinquent = Boolean.TRUE.equals(sub.getDelinquent());

        RecoverySeverity severity;

        if (delinquent || retries >= 3) {
            severity = RecoverySeverity.CRITICAL;
        } else if (sub.getStatus() == SubscriptionStatus.GRACE || retries >= 1) {
            severity = RecoverySeverity.WARNING;
        } else {
            severity = RecoverySeverity.NONE;
        }

        boolean atRisk =
                delinquent ||
                        sub.getStatus() == SubscriptionStatus.GRACE;

        return new SubscriptionResponse(
                sub.getPlan().getType(),
                sub.getStatus(),
                sub.getStartDate(),
                sub.getEndDate(),
                limits,
                features,
                delinquent,
                retries,
                atRisk,
                severity
        );
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(Authentication auth) {
        billingService.cancelSubscription(auth);
        return ResponseEntity.ok().build();
    }
}
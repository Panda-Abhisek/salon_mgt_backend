package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.exceptions.CanNotException;
import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.services.BillingService;
import com.panda.salon_mgt_backend.services.SubscriptionService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import com.panda.salon_mgt_backend.utils.subscription.SubscriptionDurations;
import com.panda.salon_mgt_backend.utils.subscription.SubscriptionPolicy;
import com.panda.salon_mgt_backend.utils.subscription.TrialPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.panda.salon_mgt_backend.models.SubscriptionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final TenantContext tenantContext;
    private final SubscriptionPolicy subscriptionPolicy;
    private final BillingService billingService;

    @Override
    public Subscription getCurrentSubscription(Authentication auth) {
        Salon salon = tenantContext.getSalon(auth);

        return subscriptionRepository
                .findTopBySalonAndStatusInOrderByStartDateDesc(
                        salon,
                        List.of(TRIAL, ACTIVE, GRACE)
                )
                .orElseThrow(() -> new IllegalStateException("No subscription found"));
    }

    @Override
    @Transactional
    public Subscription upgradePlan(Authentication auth, PlanType targetPlan) {

        Salon salon = tenantContext.getSalon(auth);

        Subscription current = subscriptionRepository
                .findTopBySalonAndStatusInOrderByStartDateDesc(
                        salon,
                        List.of(TRIAL, ACTIVE, GRACE)
                )
                .orElseThrow(() -> new IllegalStateException("No active or trial subscription found"));

        // 1️⃣ Validate upgrade rules
        subscriptionPolicy.validateUpgrade(current, targetPlan);

        // 2️⃣ Fetch target plan
        Plan newPlan = planRepository.findByType(targetPlan)
                .orElseThrow(() -> new IllegalStateException("Plan not found"));

        // 3️⃣ BILLING (new)
        BillingTransaction tx = billingService.charge(auth, newPlan);

        if (tx.getStatus() != BillingStatus.PAID) {
            throw new IllegalStateException("Payment failed");
        }

        // 4️⃣ Expire existing subscription
        if (current != null) {
            current.setStatus(SubscriptionStatus.EXPIRED);
            current.setEndDate(Instant.now());
        }

        // 5️⃣ Activate new subscription
        Instant now = Instant.now();
        Duration duration = SubscriptionDurations.durationFor(newPlan.getType());

        Subscription upgraded = Subscription.builder()
                .salon(salon)
                .plan(newPlan)
                .status(ACTIVE)
                .startDate(now)
                .endDate(now.plus(duration))
                .build();

        return subscriptionRepository.save(upgraded);
    }

    @Transactional
    @Override
    public Subscription startTrial(Authentication auth) {

        Salon salon = tenantContext.getSalon(auth);

        if (subscriptionRepository.hasUsedTrial(salon)) {
            throw new CanNotException("Trial already used for this salon");
        }

        Subscription current = subscriptionRepository
                .findBySalonAndStatus(salon, ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active subscription found"));

        // expire FREE
        current.setStatus(SubscriptionStatus.EXPIRED);
        current.setEndDate(Instant.now());

        Plan trialPlan = planRepository.findByType(PlanType.PRO).orElseThrow(() -> new IllegalStateException("Trial plan not found"));

        Instant now = Instant.now();

        Subscription trial = Subscription.builder()
                .salon(salon)
                .plan(trialPlan)
                .status(TRIAL)
                .startDate(now)
                .endDate(now.plus(TrialPolicy.TRIAL_DURATION))
                .build();

        return subscriptionRepository.save(trial);
    }
}
package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.services.SubscriptionService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import com.panda.salon_mgt_backend.utils.subscription.SubscriptionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final TenantContext tenantContext;
    private final SubscriptionPolicy subscriptionPolicy;

    @Override
    public Subscription getCurrentSubscription(Authentication auth) {
        Salon salon = tenantContext.getSalon(auth);

        return subscriptionRepository
                .findBySalonAndStatus(salon, SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

    @Override
    public Subscription upgradePlan(Authentication auth, PlanType targetPlan) {

        Salon salon = tenantContext.getSalon(auth);

        Subscription current = subscriptionRepository
                .findBySalonAndStatus(salon, SubscriptionStatus.ACTIVE)
                .orElse(null);

        // 1️⃣ Validate upgrade rules
        subscriptionPolicy.validateUpgrade(current, targetPlan);

        // 2️⃣ Expire existing subscription (if exists)
        if (current != null) {
            current.setStatus(SubscriptionStatus.EXPIRED);
            current.setEndDate(Instant.now());
        }

        // 3️⃣ Fetch target plan
        Plan newPlan = planRepository.findByType(targetPlan)
                .orElseThrow(() -> new IllegalStateException("Plan not found"));

        // 4️⃣ Create new subscription
        Subscription upgraded = Subscription.builder()
                .salon(salon)
                .plan(newPlan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(Instant.now())
                .build();

        return subscriptionRepository.save(upgraded);
    }
}
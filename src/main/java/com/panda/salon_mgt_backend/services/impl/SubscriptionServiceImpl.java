package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.exceptions.CanNotException;
import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.payloads.BillingIntentResponse;
import com.panda.salon_mgt_backend.payloads.PaymentIntent;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.services.BillingProvider;
import com.panda.salon_mgt_backend.services.BillingService;
import com.panda.salon_mgt_backend.services.SubscriptionService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import com.panda.salon_mgt_backend.utils.subscription.SubscriptionPolicy;
import com.panda.salon_mgt_backend.utils.subscription.TrialPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final BillingProvider billingProvider;
    private final BillingTransactionRepository billingRepo;

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
    public BillingIntentResponse upgradePlan(Authentication auth, PlanType targetPlan) {

        Salon salon = tenantContext.getSalon(auth);
        if (salon == null) {
            throw new CanNotException("Create a salon before upgrading plans");
        }

        Subscription current = subscriptionRepository
                .findTopBySalonAndStatusInOrderByStartDateDesc(
                        salon,
                        List.of(TRIAL, ACTIVE, GRACE)
                )
                .orElseThrow(() -> new IllegalStateException("No active subscription found"));

        // 🛑 HARD BLOCK — already on paid plan
        if (current.getStatus() == ACTIVE && current.getPlan().getType() != PlanType.FREE) {
            throw new CanNotException("You already have an active paid subscription");
        }

        // 🛑 BLOCK pending payments
        boolean hasPending = billingRepo.existsBySalonAndStatus(salon, BillingStatus.PENDING);
        if (hasPending) {
            throw new CanNotException("Payment already in progress. Please complete checkout.");
        }

        subscriptionPolicy.validateUpgrade(current, targetPlan);

        Plan newPlan = planRepository.findByType(targetPlan)
                .orElseThrow(() -> new IllegalStateException("Target plan not found"));

        PaymentIntent intent = billingService.createPayment(auth, newPlan);
        return new BillingIntentResponse(intent.checkoutUrl());
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
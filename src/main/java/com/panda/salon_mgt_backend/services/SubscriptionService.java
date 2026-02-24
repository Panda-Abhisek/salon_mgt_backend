package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.PlanType;
import com.panda.salon_mgt_backend.payloads.BillingIntentResponse;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public interface SubscriptionService {

    BillingIntentResponse upgradePlan(Authentication auth, PlanType targetPlan);

    Subscription getCurrentSubscription(Authentication auth);

    @Transactional
    Subscription startTrial(Authentication auth);
}
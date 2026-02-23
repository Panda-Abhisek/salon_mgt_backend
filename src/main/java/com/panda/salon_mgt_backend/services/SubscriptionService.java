package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.PlanType;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public interface SubscriptionService {

    Subscription upgradePlan(Authentication auth, PlanType targetPlan);

    Subscription getCurrentSubscription(Authentication auth);

    @Transactional
    Subscription startTrial(Authentication auth);
}
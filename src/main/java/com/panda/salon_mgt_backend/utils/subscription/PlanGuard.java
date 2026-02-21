package com.panda.salon_mgt_backend.utils.subscription;

import com.panda.salon_mgt_backend.exceptions.PlanUpgradeRequiredException;
import com.panda.salon_mgt_backend.models.PlanType;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.utils.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanGuard {

    private final TenantContext tenantContext;
    private final SubscriptionRepository subscriptionRepository;

    private Subscription getActiveSub(Authentication auth) {
        Salon salon = tenantContext.getSalon(auth);

        return subscriptionRepository
                .findBySalonAndStatus(salon, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active subscription"));
    }

    // --------- CHECKERS ---------

    public boolean isFree(Authentication auth) {
        return getActiveSub(auth).getPlan().getType() == PlanType.FREE;
    }

    public boolean isPro(Authentication auth) {
        PlanType code = getActiveSub(auth).getPlan().getType();
        return code == PlanType.PRO || code == PlanType.PREMIUM;
    }

    public boolean isPremium(Authentication auth) {
        return getActiveSub(auth).getPlan().getType() == PlanType.PREMIUM;
    }

    public PlanType currentPlan(Authentication auth) {
        return getActiveSub(auth).getPlan().getType();
    }

    // --------- ENFORCERS (🔥 PAYWALL METHODS) ---------

    public void requirePro(Authentication auth) {
        if (!isPro(auth)) {
            throw new PlanUpgradeRequiredException(PlanType.PRO);
        }
    }

    public void requirePremium(Authentication auth) {
        if (!isPremium(auth)) {
            throw new PlanUpgradeRequiredException(PlanType.PREMIUM);
        }
    }

    public void requireNotFree(Authentication auth) {
        if (isFree(auth)) {
            throw new AccessDeniedException("Upgrade required to access this feature");
        }
    }
}
package com.panda.salon_mgt_backend.utils.subscription;

import com.panda.salon_mgt_backend.models.PlanType;
import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionPolicy {

    /**
     * Validate if upgrade is allowed.
     * Throws IllegalStateException for invalid transitions.
     */
    public void validateUpgrade(Subscription current, PlanType targetPlan) {
        if (current == null) {
            // No subscription yet → allow
            return;
        }

        PlanType currentPlan = current.getPlan().getType();

        // Same plan upgrade
        if (currentPlan == targetPlan) {
            throw new IllegalStateException("Already on this plan");
        }

        // ❌ Downgrade protection
        if (isDowngrade(currentPlan, targetPlan)) {
            throw new IllegalStateException("Downgrades are not allowed");
        }

        // If subscription expired → allow upgrade
        if (current.getStatus() == SubscriptionStatus.EXPIRED
                || current.getStatus() == SubscriptionStatus.CANCELLED) {
            return;
        }

        // ACTIVE upgrades are allowed only upward
        // FREE → PRO → PREMIUM
    }

    private boolean isDowngrade(PlanType current, PlanType target) {
        return rank(target) < rank(current);
    }

    private int rank(PlanType plan) {
        return switch (plan) {
            case FREE -> 1;
            case PRO -> 2;
            case PREMIUM -> 3;
        };
    }
}
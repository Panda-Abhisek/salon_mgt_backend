package com.panda.salon_mgt_backend.exceptions;

import com.panda.salon_mgt_backend.models.PlanType;
import lombok.Getter;

@Getter
public class PlanUpgradeRequiredException extends RuntimeException {

    private final PlanType requiredPlan;

    public PlanUpgradeRequiredException(PlanType requiredPlan) {
        super("Upgrade required to access this feature");
        this.requiredPlan = requiredPlan;
    }

    public PlanUpgradeRequiredException(String message, PlanType requiredPlan) {
        super(message);
        this.requiredPlan = requiredPlan;
    }
}
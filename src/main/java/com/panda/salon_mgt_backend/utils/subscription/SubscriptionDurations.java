package com.panda.salon_mgt_backend.utils.subscription;

import com.panda.salon_mgt_backend.models.PlanType;

import java.time.Duration;

public class SubscriptionDurations {
    public static Duration durationFor(PlanType type) {
        return switch (type) {
            case FREE -> Duration.ofDays(3650); // effectively unlimited
            case PRO, PREMIUM -> Duration.ofDays(30);
        };
    }
}
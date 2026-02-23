package com.panda.salon_mgt_backend.payloads;

import com.panda.salon_mgt_backend.models.SubscriptionStatus;

public record SubscriptionLifecycleResponse(
        SubscriptionStatus status,
        long daysRemaining,
        boolean endingSoon,
        boolean inGrace,
        boolean inTrial
) {}
package com.panda.salon_mgt_backend.payloads;

import com.panda.salon_mgt_backend.models.PlanType;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionResponse(
        PlanType plan,
        SubscriptionStatus status,
        Instant startDate,
        Instant endDate
) {}
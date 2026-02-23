package com.panda.salon_mgt_backend.payloads;

import com.panda.salon_mgt_backend.models.PlanType;

import java.util.Map;

public record BillingMetricsResponse(
        long totalActive,
        Map<PlanType, Long> activeByPlan,
        long expiringSoon,
        long expiredLast7Days,
        ChurnDto churn,
        ConversionMetrics conversion
) {}
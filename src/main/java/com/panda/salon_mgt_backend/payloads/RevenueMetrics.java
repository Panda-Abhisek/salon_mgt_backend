package com.panda.salon_mgt_backend.payloads;

import lombok.Builder;

@Builder
public record RevenueMetrics(
        long activeSubscriptions,
        long activePaidSubscriptions,
        long activeFreeSubscriptions,

        long monthlyRecurringRevenue,
        long previousMonthMRR,
        long netRevenueChange,
        double mrrGrowthRate,

        double paidConversionRate,
        double arpu,

        long newPaidActivationsLast30Days,
        long churnedLast30Days
) {}
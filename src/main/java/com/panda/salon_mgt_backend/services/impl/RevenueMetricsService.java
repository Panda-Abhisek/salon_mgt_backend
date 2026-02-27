package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.models.PlanType;
import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;
import com.panda.salon_mgt_backend.payloads.RevenueMetrics;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueMetricsService {

    private final SubscriptionRepository subscriptionRepository;

    public RevenueMetrics calculate() {

        List<Subscription> all = subscriptionRepository.findAll();

        List<Subscription> active = all.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();

        long total = active.size();

        long paid = active.stream()
                .filter(s -> s.getPlan().getType() != PlanType.FREE)
                .count();

        long free = total - paid;

        long mrr = active.stream()
                .filter(s -> s.getPlan().getType() != PlanType.FREE)
                .mapToLong(s -> s.getPlan().getPriceMonthly())
                .sum();

        Instant now = Instant.now();
        Instant lastMonth = now.minus(30, ChronoUnit.DAYS);

        long prevMRR = calculateMRRAt(lastMonth, all);

        long netChange = mrr - prevMRR;

        double growth = prevMRR == 0 ? 0 : (double) netChange / prevMRR;

        double conversion = total == 0 ? 0 : (double) paid / total;

        double arpu = paid == 0 ? 0 : (double) mrr / paid;

        long newPaid = active.stream()
                .filter(s -> s.getPlan().getType() != PlanType.FREE)
                .filter(s -> s.getStartDate().isAfter(lastMonth))
                .count();

        long churn = all.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.EXPIRED)
                .filter(s -> s.getEndDate() != null && s.getEndDate().isAfter(lastMonth))
                .count();

        return RevenueMetrics.builder()
                .activeSubscriptions(total)
                .activePaidSubscriptions(paid)
                .activeFreeSubscriptions(free)
                .monthlyRecurringRevenue(mrr)
                .previousMonthMRR(prevMRR)
                .netRevenueChange(netChange)
                .mrrGrowthRate(growth)
                .paidConversionRate(conversion)
                .arpu(arpu)
                .newPaidActivationsLast30Days(newPaid)
                .churnedLast30Days(churn)
                .build();
    }

    private long calculateMRRAt(Instant cutoff, List<Subscription> subs) {
        return subs.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .filter(s -> s.getPlan().getType() != PlanType.FREE)
                .filter(s -> s.getStartDate().isBefore(cutoff))
                .mapToLong(s -> s.getPlan().getPriceMonthly())
                .sum();
    }
}
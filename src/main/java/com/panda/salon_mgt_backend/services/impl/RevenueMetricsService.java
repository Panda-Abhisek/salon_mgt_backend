package com.panda.salon_mgt_backend.services.impl;

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

        List<Subscription> activeSubs = subscriptionRepository.findAll()
                .stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();

        long total = activeSubs.size();

        long paid = activeSubs.stream()
                .filter(s -> s.getPlan().getType() != com.panda.salon_mgt_backend.models.PlanType.FREE)
                .count();

        long free = total - paid;

        long mrr = activeSubs.stream()
                .filter(s -> s.getPlan().getType() != com.panda.salon_mgt_backend.models.PlanType.FREE)
                .mapToLong(s -> s.getPlan().getPriceMonthly())
                .sum();

        Instant cutoff = Instant.now().minus(30, ChronoUnit.DAYS);

        long newPaid = activeSubs.stream()
                .filter(s -> s.getPlan().getType() != com.panda.salon_mgt_backend.models.PlanType.FREE)
                .filter(s -> s.getStartDate().isAfter(cutoff))
                .count();

        long churn = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.EXPIRED)
                .filter(s -> s.getEndDate() != null && s.getEndDate().isAfter(cutoff))
                .count();

        return RevenueMetrics.builder()
                .activeSubscriptions(total)
                .activePaidSubscriptions(paid)
                .activeFreeSubscriptions(free)
                .monthlyRecurringRevenue(mrr)
                .newPaidActivationsLast30Days(newPaid)
                .churnedLast30Days(churn)
                .build();
    }
}
package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.payloads.RevenuePoint;
import com.panda.salon_mgt_backend.models.PlanType;
import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueTimelineService {

    private final SubscriptionRepository repo;

    public List<RevenuePoint> getTimeline(int days) {

        List<Subscription> all = repo.findAll();
        List<RevenuePoint> result = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = days; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Instant cutoff = date.atStartOfDay().toInstant(ZoneOffset.UTC);

            long mrr = all.stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                    .filter(s -> s.getPlan().getType() != PlanType.FREE)
                    .filter(s -> s.getStartDate().isBefore(cutoff))
                    .mapToLong(s -> s.getPlan().getPriceMonthly())
                    .sum();

            result.add(new RevenuePoint(date.toString(), mrr));
        }

        return result;
    }
}
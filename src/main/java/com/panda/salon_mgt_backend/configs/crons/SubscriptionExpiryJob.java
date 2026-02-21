package com.panda.salon_mgt_backend.configs.crons;

import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiryJob {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    @Transactional
    @Scheduled(cron = "0 0 2 * * *") // daily at 2AM
    public void expireSubscriptions() {

        Instant now = Instant.now();

        List<Subscription> expired = subscriptionRepository
                .findAll().stream()
                .filter(sub ->
                        sub.getStatus() == SubscriptionStatus.ACTIVE &&
                        sub.getEndDate() != null &&
                        sub.getEndDate().isBefore(now)
                )
                .toList();

        if (expired.isEmpty()) return;

        Plan freePlan = planRepository.findByType(PlanType.FREE)
                .orElseThrow();

        for (Subscription sub : expired) {
            Salon salon = sub.getSalon();
            PlanType oldPlan = sub.getPlan().getType();
            Long salonId = salon.getSalonId();

            // 1️⃣ Mark expired
            sub.setStatus(SubscriptionStatus.EXPIRED);

            log.info("subscription.expired salonId={} plan={} expiredAt={}",
                    salonId,
                    oldPlan,
                    Instant.now()
            );

            // 2️⃣ Assign FREE fallback
            Subscription fallback = Subscription.builder()
                    .salon(salon)
                    .plan(freePlan)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(now)
                    .endDate(now.plus(Duration.ofDays(3650)))
                    .build();

            subscriptionRepository.save(fallback);

            log.info("subscription.fallback salonId={} newPlan=FREE activatedAt={}",
                    salonId,
                    now
            );
        }

        log.info("Expired {} subscriptions", expired.size());
    }
}
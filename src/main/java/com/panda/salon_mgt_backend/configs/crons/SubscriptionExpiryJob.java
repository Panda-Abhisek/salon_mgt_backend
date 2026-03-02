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

    private static final Duration GRACE_WINDOW = Duration.ofDays(7);

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    @Transactional
    @Scheduled(cron = "0 0 2 * * *")
    public void expireSubscriptions() {
        log.info("subscription.expiry.job.started");
        Instant now = Instant.now();

        List<Subscription> subs = subscriptionRepository.findAll();
        log.debug("subscription.expiry.scan_size={}", subs.size());
        Plan freePlan = planRepository.findByType(PlanType.FREE)
                .orElseThrow();

        for (Subscription sub : subs) {

            if (sub.getEndDate() == null) continue;

            // TRIAL expiry (no grace)
            if (sub.getStatus() == SubscriptionStatus.TRIAL &&
                    sub.getEndDate().isBefore(now)) {

                sub.setStatus(SubscriptionStatus.EXPIRED);

                log.warn(
                        "subscription.trial_expired salonId={} trialEnd={} fallbackPlan=FREE",
                        sub.getSalon().getSalonId(),
                        sub.getEndDate()
                );

                // FREE fallback
                Subscription fallback = Subscription.builder()
                        .salon(sub.getSalon())
                        .plan(freePlan)
                        .status(SubscriptionStatus.ACTIVE)
                        .startDate(now)
                        .endDate(now.plus(Duration.ofDays(3650)))
                        .build();

                subscriptionRepository.save(fallback);

                continue;
            }

            // -------------------------------
            // 1️⃣ ACTIVE → GRACE
            // -------------------------------
            if (sub.getStatus() == SubscriptionStatus.ACTIVE &&
                    sub.getEndDate().isBefore(now)) {

                sub.setStatus(SubscriptionStatus.GRACE);

                log.warn(
                        "subscription.entered_grace salonId={} plan={} graceStart={} originalEnd={}",
                        sub.getSalon().getSalonId(),
                        sub.getPlan().getType(),
                        now,
                        sub.getEndDate()
                );
            }

            // -------------------------------
            // 2️⃣ GRACE → EXPIRED + FREE fallback
            // -------------------------------
            else if (sub.getStatus() == SubscriptionStatus.GRACE) {

                Instant graceEnd = sub.getEndDate().plus(GRACE_WINDOW);

                if (graceEnd.isBefore(now)) {

                    Salon salon = sub.getSalon();
                    PlanType oldPlan = sub.getPlan().getType();

                    sub.setStatus(SubscriptionStatus.EXPIRED);

                    log.error(
                            "subscription.expired_after_grace salonId={} plan={} expiredAt={} graceWindowDays=7",
                            salon.getSalonId(),
                            oldPlan,
                            now
                    );

                    // FREE fallback
                    Subscription fallback = Subscription.builder()
                            .salon(salon)
                            .plan(freePlan)
                            .status(SubscriptionStatus.ACTIVE)
                            .startDate(now)
                            .endDate(now.plus(Duration.ofDays(3650)))
                            .build();

                    subscriptionRepository.save(fallback);

                    log.warn(
                            "subscription.fallback_to_free salonId={} fromPlan={} activatedAt={}",
                            salon.getSalonId(),
                            oldPlan,
                            now
                    );

                    log.info("subscription.expiry.job.completed");
                }
            }
        }
    }
}
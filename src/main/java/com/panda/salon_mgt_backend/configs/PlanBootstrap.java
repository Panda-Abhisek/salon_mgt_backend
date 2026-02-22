package com.panda.salon_mgt_backend.configs;

import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.PlanType;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class PlanBootstrap implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    public void run(String... args) {

        upsertPlan(
                PlanType.FREE,
                Plan.builder()
                        .type(PlanType.FREE)
                        .name("Free")
                        .maxStaff(2)
                        .maxServices(5)
                        .maxBookings(50)
                        .analyticsEnabled(false)
                        .smartAlertsEnabled(false)
                        .priceMonthly(0)
                        .build()
        );

        upsertPlan(
                PlanType.PRO,
                Plan.builder()
                        .type(PlanType.PRO)
                        .name("Pro")
                        .maxStaff(10)
                        .maxServices(25)
                        .maxBookings(300)
                        .analyticsEnabled(true)
                        .smartAlertsEnabled(false) // keep premium feature gated
                        .priceMonthly(999)
                        .build()
        );

        upsertPlan(
                PlanType.PREMIUM,
                Plan.builder()
                        .type(PlanType.PREMIUM)
                        .name("Premium")
                        .maxStaff(50)
                        .maxServices(100)
                        .maxBookings(1000)
                        .analyticsEnabled(true)
                        .smartAlertsEnabled(true)
                        .priceMonthly(2499)
                        .build()
        );
    }

    private void createIfMissing(PlanType type, Plan plan) {
        planRepository.findByType(type)
                .orElseGet(() -> planRepository.save(plan));
    }

    private void upsertPlan(PlanType type, Plan desired) {
        planRepository.findByType(type).ifPresentOrElse(existing -> {
            boolean changed = false;

            if (!Objects.equals(existing.getMaxStaff(), desired.getMaxStaff())) {
                existing.setMaxStaff(desired.getMaxStaff());
                changed = true;
            }
            if (!Objects.equals(existing.getMaxServices(), desired.getMaxServices())) {
                existing.setMaxServices(desired.getMaxServices());
                changed = true;
            }
            if (!Objects.equals(existing.getMaxBookings(), desired.getMaxBookings())) {
                existing.setMaxBookings(desired.getMaxBookings());
                changed = true;
            }
            if (!Objects.equals(existing.getAnalyticsEnabled(), desired.getAnalyticsEnabled())) {
                existing.setAnalyticsEnabled(desired.getAnalyticsEnabled());
                changed = true;
            }
            if (!Objects.equals(existing.getSmartAlertsEnabled(), desired.getSmartAlertsEnabled())) {
                existing.setSmartAlertsEnabled(desired.getSmartAlertsEnabled());
                changed = true;
            }
            if (!Objects.equals(existing.getPriceMonthly(), desired.getPriceMonthly())) {
                existing.setPriceMonthly(desired.getPriceMonthly());
                changed = true;
            }

            if (changed) {
                log.info("plan.updated type={} updated=true", type);
                planRepository.save(existing);
            }
        }, () -> planRepository.save(desired));
    }
}
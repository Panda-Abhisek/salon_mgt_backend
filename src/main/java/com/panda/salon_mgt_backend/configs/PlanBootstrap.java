package com.panda.salon_mgt_backend.configs;

import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PlanBootstrap implements CommandLineRunner {

    private final PlanRepository planRepository;

    @Override
    public void run(String... args) {

        createIfMissing(
                PlanType.FREE,
                Plan.builder()
                        .type(PlanType.FREE)
                        .name("Free")
                        .maxStaff(2)
                        .analyticsEnabled(false)
                        .smartAlertsEnabled(false)
                        .priceMonthly(0)
                        .build()
        );

        createIfMissing(
                PlanType.PRO,
                Plan.builder()
                        .type(PlanType.PRO)
                        .name("Pro")
                        .maxStaff(10)
                        .analyticsEnabled(true)
                        .smartAlertsEnabled(true)
                        .priceMonthly(999)
                        .build()
        );

        createIfMissing(
                PlanType.PREMIUM,
                Plan.builder()
                        .type(PlanType.PREMIUM)
                        .name("Premium")
                        .maxStaff(50)
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
}
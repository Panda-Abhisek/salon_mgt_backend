package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.payloads.BillingInsightsDto;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.services.BillingInsightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingInsightsServiceImpl implements BillingInsightsService {

    private final SubscriptionRepository repo;

    @Override
    public BillingInsightsDto getBillingHealth() {
        log.info("billing.insights.compute.started");

        long activePaid = repo.countActivePaid();
        long grace = repo.countInGrace();
        long delinquent = repo.countDelinquent();
        long atRisk = repo.countAtRisk();

        log.debug(
                "billing.insights.snapshot activePaid={} grace={} delinquent={} atRisk={}",
                activePaid,
                grace,
                delinquent,
                atRisk
        );

        log.info("billing.insights.compute.completed");

        return new BillingInsightsDto(
                activePaid,
                grace,
                delinquent,
                atRisk
        );
    }
}
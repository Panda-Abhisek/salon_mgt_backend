package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.payloads.BillingInsightsDto;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.services.BillingInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillingInsightsServiceImpl implements BillingInsightsService {

    private final SubscriptionRepository repo;

    @Override
    public BillingInsightsDto getBillingHealth() {
        return new BillingInsightsDto(
                repo.countActivePaid(),
                repo.countInGrace(),
                repo.countDelinquent(),
                repo.countAtRisk()
        );
    }
}
package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.models.BillingStatus;
import com.panda.salon_mgt_backend.payloads.BillingObservabilityDto;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BillingObservabilityService {

    private final BillingTransactionRepository billingRepo;
    private final SubscriptionRepository subscriptionRepo;

    public BillingObservabilityDto snapshot() {

        long pending = billingRepo.countByStatus(BillingStatus.PENDING);
        long deadLetters = billingRepo.countDeadLetters();

        Instant today = Instant.now().minus(1, ChronoUnit.DAYS);
        long recoveredToday = billingRepo.countRecoveredSince(today);

        long attempts = recoveredToday + deadLetters;
        double recoveryRate = attempts == 0 ? 1.0 : (double) recoveredToday / attempts;

        long atRisk = subscriptionRepo.countAtRisk();

        return BillingObservabilityDto.builder()
                .pending(pending)
                .deadLetters(deadLetters)
                .recoveredToday(recoveredToday)
                .recoveryRate(recoveryRate)
                .atRisk(atRisk)
                .build();
    }
}
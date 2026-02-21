package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.configs.MockPaymentProcessor;
import com.panda.salon_mgt_backend.models.BillingStatus;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import com.panda.salon_mgt_backend.services.BillingService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final TenantContext tenantContext;
    private final BillingTransactionRepository billingRepo;
    private final MockPaymentProcessor paymentProcessor;

    @Override
    public BillingTransaction charge(Authentication auth, Plan plan) {

        Salon salon = tenantContext.getSalon(auth);

        BillingTransaction tx = BillingTransaction.builder()
                .salon(salon)
                .planType(plan.getType())
                .status(BillingStatus.PENDING)
                .amount(plan.getPriceMonthly())
                .createdAt(Instant.now())
                .build();

        tx = billingRepo.save(tx);

        boolean success = paymentProcessor.processPayment(tx.getAmount());

        if (success) {
            tx.setStatus(BillingStatus.PAID);
            tx.setPaidAt(Instant.now());
        } else {
            tx.setStatus(BillingStatus.FAILED);
        }

        return billingRepo.save(tx);
    }
}
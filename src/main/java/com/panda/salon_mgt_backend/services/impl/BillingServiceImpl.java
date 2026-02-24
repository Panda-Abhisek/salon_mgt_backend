package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.configs.billing.BillingProviderFactory;
import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.payloads.CheckoutSession;
import com.panda.salon_mgt_backend.payloads.PaymentIntent;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.services.BillingProvider;
import com.panda.salon_mgt_backend.services.BillingService;
import com.panda.salon_mgt_backend.utils.TenantContext;
import com.panda.salon_mgt_backend.utils.subscription.SubscriptionDurations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.panda.salon_mgt_backend.models.SubscriptionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final TenantContext tenantContext;
    private final BillingTransactionRepository billingRepo;
    private final BillingProviderFactory providerFactory;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    @Override
    @Transactional
    public PaymentIntent createPayment(Authentication auth, Plan newPlan) {

        Salon salon = tenantContext.getSalon(auth);

        if (salon == null) {
            throw new IllegalStateException("Cannot create payment without a salon");
        }

        BillingProvider billingProvider = providerFactory.get();

        BillingTransaction tx = new BillingTransaction();
        tx.setSalon(salon);
        tx.setPlan(newPlan.getType());
        tx.setAmount(newPlan.getPriceMonthly());
        tx.setStatus(BillingStatus.CREATED);
        tx.setProvider(billingProvider.name());
        tx.setCreatedAt(Instant.now());

        billingRepo.save(tx);

        CheckoutSession session = billingProvider.createCheckout(salon, newPlan, tx);

        tx.setExternalOrderId(session.externalOrderId());
        tx.setStatus(BillingStatus.PENDING);

        billingRepo.save(tx);
        log.info("billing.intent.created salonId={} plan={} amount={}",
                salon.getSalonId(),
                newPlan.getType(),
                newPlan.getPriceMonthly()
        );
        return new PaymentIntent(tx, session.checkoutUrl());
    }

    @Transactional
    @Override
    public void handlePaymentResult(BillingResult result) {

        BillingTransaction tx = billingRepo
                .findByExternalOrderId(result.externalOrderId())
                .orElseThrow(() -> new IllegalStateException("Transaction not found"));
        if (tx.getStatus() == BillingStatus.PAID) {
            log.info("Webhook replay ignored orderId={}", tx.getExternalOrderId());
            return;
        }
        if (tx.getStatus() != BillingStatus.PENDING) {
            log.warn("Invalid payment state transition orderId={} status={}",
                    tx.getExternalOrderId(),
                    tx.getStatus());
            return;
        }
        if (!result.success()) {
            tx.setStatus(BillingStatus.FAILED);
            log.warn("billing.payment.failed orderId={}", result.externalOrderId());
            return;
        }

        // Mark transaction paid
        tx.setStatus(BillingStatus.PAID);
        tx.setExternalPaymentId(result.externalPaymentId());
        tx.setCompletedAt(Instant.now());

        log.info("billing.payment.success orderId={} paymentId={} salonId={}",
                tx.getExternalOrderId(),
                tx.getExternalPaymentId(),
                tx.getSalon().getSalonId()
        );

        activateSubscription(tx); // 🔥 THE BRIDGE
    }

    @Transactional
    void activateSubscription(BillingTransaction tx) {

        Salon salon = tx.getSalon();

        // expire existing sub
        subscriptionRepository
                .findTopBySalonAndStatusInOrderByStartDateDesc(
                        salon,
                        List.of(TRIAL, ACTIVE, GRACE)
                )
                .ifPresent(current -> {
                    current.setStatus(SubscriptionStatus.EXPIRED);
                    current.setEndDate(Instant.now());
                });

        Plan plan = planRepository.findByType(tx.getPlan())
                .orElseThrow();

        Instant now = Instant.now();
        Duration duration = SubscriptionDurations.durationFor(plan.getType());

        Subscription newSub = Subscription.builder()
                .salon(salon)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(now.plus(duration))
                .externalPaymentId(tx.getExternalPaymentId())
                .build();

        subscriptionRepository.save(newSub);
    }
}
package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.configs.billing.BillingProviderFactory;
import com.panda.salon_mgt_backend.models.*;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.payloads.CheckoutSession;
import com.panda.salon_mgt_backend.payloads.PaymentIntent;
import com.panda.salon_mgt_backend.repositories.BillingTransactionRepository;
import com.panda.salon_mgt_backend.repositories.PlanRepository;
import com.panda.salon_mgt_backend.repositories.StripeWebhookEventRepository;
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
    private final StripeWebhookEventRepository webhookRepo;

    @Override
    @Transactional
    public PaymentIntent createPayment(Authentication auth, Plan newPlan) {

        Salon salon = tenantContext.getSalon(auth);
        log.info("Salon: {}", salon);
        log.info("Plan price: {}", newPlan.getPriceMonthly());

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

    @Override
    @Transactional
    public void handlePaymentResult(BillingResult result) {

        String eventId = result.stripeEventId();

        // Webhook idempotency
        if (eventId != null && webhookRepo.existsByStripeEventId(eventId)) {
            log.warn("billing.webhook.replay eventId={}", eventId);
            return;
        }

        // ----------------------------------
        // 🔁 RENEWAL FLOW
        // ----------------------------------
        if (result.txId() == null && result.stripeSubscriptionId() != null) {
            handleRenewal(result);
            persistWebhook(eventId);
            return;
        }

        // ----------------------------------
        // 🆕 ACTIVATION FLOW (existing)
        // ----------------------------------
        BillingTransaction tx = billingRepo
                .findById(Long.parseLong(result.txId()))
                .orElseThrow(() -> new IllegalStateException("Transaction not found"));

        if (tx.getStatus() == BillingStatus.PAID) {
            log.info("billing.payment.already_processed txId={}", tx.getId());
            return;
        }

        tx.setStatus(BillingStatus.PAID);
        tx.setInitialPaymentIntentId(result.externalPaymentId());
        tx.setCompletedAt(Instant.now());
        billingRepo.save(tx);

        activateSubscription(tx, result);
        persistWebhook(eventId);

        log.info("billing.activation.confirmed txId={}", tx.getId());
    }

    @Transactional
    void activateSubscription(BillingTransaction tx, BillingResult result) {

        Salon salon = tx.getSalon();

        subscriptionRepository
                .findTopBySalonAndStatusInOrderByStartDateDesc(
                        salon,
                        List.of(TRIAL, ACTIVE, GRACE)
                )
                .ifPresent(current -> {
                    current.setStatus(SubscriptionStatus.EXPIRED);
                    current.setEndDate(Instant.now());
                });

        Plan plan = planRepository.findByType(tx.getPlan()).orElseThrow();

        Instant now = Instant.now();
        Duration duration = SubscriptionDurations.durationFor(plan.getType());

        Subscription newSub = Subscription.builder()
                .salon(salon)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(now.plus(duration))
                .externalPaymentId(tx.getInitialPaymentIntentId())

                // 🔥 RECURRING FIELDS
                .stripeCustomerId(result.stripeCustomerId())
                .stripeSubscriptionId(result.stripeSubscriptionId())

                .build();

        subscriptionRepository.save(newSub);
    }

    @Transactional
    void handleRenewal(BillingResult result) {

        String stripeSubId = result.stripeSubscriptionId();

        Subscription sub = subscriptionRepository
                .findAll()
                .stream()
                .filter(s -> stripeSubId.equals(s.getStripeSubscriptionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Subscription not found for Stripe sub=" + stripeSubId));

        // Extend duration
        Duration duration = SubscriptionDurations.durationFor(sub.getPlan().getType());

        Instant newEnd = sub.getEndDate().plus(duration);
        sub.setEndDate(newEnd);

        log.info("billing.renewal.applied salonId={} newEnd={}",
                sub.getSalon().getSalonId(),
                newEnd);
    }

    private void persistWebhook(String eventId) {
        if (eventId == null) return;

        webhookRepo.save(
                StripeWebhookEvent.builder()
                        .stripeEventId(eventId)
                        .processedAt(Instant.now())
                        .build()
        );
    }

    @Transactional
    @Override
    public void cancelSubscription(Authentication auth) {

        Salon salon = tenantContext.getSalon(auth);

        Subscription sub = subscriptionRepository
                .findTopBySalonAndStatusInOrderByStartDateDesc(
                        salon,
                        List.of(ACTIVE, GRACE)
                )
                .orElseThrow(() -> new IllegalStateException("No active subscription"));

        if (sub.getStripeSubscriptionId() == null) {
            throw new IllegalStateException("Not a Stripe subscription");
        }

        try {
            com.stripe.model.Subscription stripeSub =
                    com.stripe.model.Subscription.retrieve(sub.getStripeSubscriptionId());

            com.stripe.param.SubscriptionUpdateParams params =
                    com.stripe.param.SubscriptionUpdateParams.builder()
                            .setCancelAtPeriodEnd(true)
                            .build();

            stripeSub.update(params);

            sub.setCancelAtPeriodEnd(true);
            subscriptionRepository.save(sub);

            log.info("billing.cancel_requested salonId={} stripeSub={}",
                    salon.getSalonId(),
                    sub.getStripeSubscriptionId());

        } catch (Exception e) {
            throw new RuntimeException("Stripe cancellation failed", e);
        }
    }
}
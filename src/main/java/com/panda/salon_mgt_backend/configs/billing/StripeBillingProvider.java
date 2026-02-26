package com.panda.salon_mgt_backend.configs.billing;

import com.panda.salon_mgt_backend.models.BillingProviderType;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.payloads.CheckoutSession;
import com.panda.salon_mgt_backend.services.BillingProvider;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class StripeBillingProvider implements BillingProvider {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final StripePriceConfig priceConfig;

    @PostConstruct
    void init() {
        Stripe.apiKey = secretKey;
    }

    @Override
    public BillingProviderType name() {
        return BillingProviderType.STRIPE;
    }

    @Override
    public CheckoutSession createCheckout(Salon salon, Plan plan, BillingTransaction tx) {
        try {
            String priceId = priceConfig.getPriceId(plan.getType());

            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.SUBSCRIPTION) // 🔥 IMPORTANT
                            .setSuccessUrl("http://localhost:5173/billing/success")
                            .setCancelUrl("http://localhost:5173/billing")
                            .putMetadata("txId", tx.getId().toString())
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setPrice(priceId) // 🔥 STRIPE PRICE ID
                                            .setQuantity(1L)
                                            .build()
                            )
                            .build();

            Session session = Session.create(params);

            return new CheckoutSession(session.getUrl(), session.getId());

        } catch (Exception e) {
            log.error("Stripe error", e);
            throw new RuntimeException("Stripe checkout failed", e);
        }
    }

    @Override
    public BillingResult verifyPayment(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);

            // Stripe noise → DEBUG only
            log.debug("Stripe webhook received type={}", event.getType());

            // Ignore non-checkout events silently
            if (!"checkout.session.completed".equals(event.getType())) {
                return new BillingResult(null, null, null, null, null, false, true);
            }

            var deserializer = event.getDataObjectDeserializer();
            Session rawSession = (Session) deserializer.deserializeUnsafe();

            String txId = rawSession.getMetadata().get("txId");

            if (txId == null) {
                log.warn("Stripe session missing txId metadata sessionId={}", rawSession.getId());
                return new BillingResult(null, null, null, null, null, false, true);
            }

            // Expand subscription tree (subscription mode)
            Session session = Session.retrieve(
                    rawSession.getId(),
                    com.stripe.param.checkout.SessionRetrieveParams.builder()
                            .addExpand("subscription")
                            .addExpand("subscription.latest_invoice")
                            .addExpand("subscription.latest_invoice.payment_intent")
                            .build(),
                    null
            );

            String customerId = session.getCustomer();
            String subscriptionId = session.getSubscription();

            String paymentIntentId = null;

            if (session.getSubscriptionObject() != null
                    && session.getSubscriptionObject().getLatestInvoiceObject() != null
                    && session.getSubscriptionObject()
                    .getLatestInvoiceObject()
                    .getPaymentIntentObject() != null) {

                paymentIntentId =
                        session.getSubscriptionObject()
                                .getLatestInvoiceObject()
                                .getPaymentIntentObject()
                                .getId();
            }

            // Only meaningful billing log → INFO
            log.info("Stripe checkout completed txId={} paymentIntent={} subscriptionId={}",
                    txId, paymentIntentId, subscriptionId);

            return new BillingResult(
                    txId,
                    paymentIntentId,
                    customerId,
                    subscriptionId,
                    event.getId(),
                    true,
                    false
            );

        } catch (Exception e) {
            // Real failure → ERROR
            log.error("Stripe webhook verification failed", e);
            return new BillingResult(null, null, null, null, null, false, true);
        }
    }
}
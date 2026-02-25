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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary
public class StripeBillingProvider implements BillingProvider {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

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
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setMode(SessionCreateParams.Mode.PAYMENT)
                            .setSuccessUrl("http://localhost:5173/billing/success")
                            .setCancelUrl("http://localhost:5173/billing")
                            .putMetadata("txId", tx.getId().toString())   // MUST be here
                            .addLineItem(
                                    SessionCreateParams.LineItem.builder()
                                            .setQuantity(1L)
                                            .setPriceData(
                                                    SessionCreateParams.LineItem.PriceData.builder()
                                                            .setCurrency("inr")
                                                            .setUnitAmount(plan.getPriceMonthly() * 100L)
                                                            .setProductData(
                                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                            .setName(plan.getName())
                                                                            .build()
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            Session session = Session.create(params);
            log.info("Stripe key length: {}", secretKey.length());
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
            log.info("Stripe event received type={} id={}", event.getType(), event.getId());

            if (!"checkout.session.completed".equals(event.getType())) {
                return new BillingResult(null, null, false, true);
            }

            var deserializer = event.getDataObjectDeserializer();

            if (deserializer.getObject().isEmpty()) {
                log.warn("Stripe object not in SDK schema, using unsafe deserialization");
            }

            Session session = (Session) deserializer.deserializeUnsafe();

            String txId = session.getMetadata().get("txId");

            if (txId == null) {
                log.error("🚨 Stripe session missing txId metadata. Session={}", session.getId());
                return new BillingResult(null, null, false, false);
            }

            return new BillingResult(
                    txId,                           // 🔥 internal ID
                    session.getPaymentIntent(),   // Stripe payment id
                    true,
                    false
            );

        } catch (Exception e) {
            log.error("Stripe webhook verification failed", e);
            return new BillingResult(null, null, false, false);
        }
    }
}
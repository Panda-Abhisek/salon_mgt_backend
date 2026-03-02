package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.configs.billing.BillingProviderFactory;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.services.BillingProvider;
import com.panda.salon_mgt_backend.services.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/webhook")
@RequiredArgsConstructor
@Slf4j
public class BillingWebhookController {

    private final BillingProviderFactory providerFactory;
    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        // Keep webhook entry at DEBUG (Stripe is noisy)
        log.debug("stripe.webhook.received");

        BillingProvider provider = providerFactory.get();
        BillingResult result = provider.verifyPayment(payload, signature);

        // Ignore non-checkout events silently
        if (result.isIgnored()) {
            log.debug("stripe.webhook.ignored");
            return ResponseEntity.ok().build();
        }

        // Verification failed (signature or parsing issue)
        if (!result.success()) {
            log.warn("stripe.webhook.verification_failed");
            return ResponseEntity.ok().build();
        }

        // Process billing
        billingService.handlePaymentResult(result);

        // Single meaningful billing log
        log.info("stripe.webhook.processed txId={} paymentIntent={} stripeSub={}",
                result.txId(),
                result.externalPaymentId(),
                result.stripeSubscriptionId()
        );

        return ResponseEntity.ok().build();
    }
}
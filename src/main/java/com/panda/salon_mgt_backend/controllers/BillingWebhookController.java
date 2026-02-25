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
        log.info("🔥 Webhook endpoint hit");

        BillingProvider provider = providerFactory.get();
        BillingResult result = provider.verifyPayment(payload, signature);

        log.error("WEBHOOK DEBUG -> ignored={}, success={}, txId={}",
                result.isIgnored(),
                result.success(),
                result.txId());

        if (result.isIgnored()) {
            log.info("Ignoring non-checkout event");
            return ResponseEntity.ok().build();
        }

        if (!result.success()) {   // ✅ FIXED
            log.warn("Webhook verification failed");
            return ResponseEntity.ok().build();
        }

        billingService.handlePaymentResult(result);
        log.info("✅ Payment processed: {}", result.txId());

        return ResponseEntity.ok().build();
    }
}
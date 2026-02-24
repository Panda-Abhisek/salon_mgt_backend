package com.panda.salon_mgt_backend.controllers;

import com.panda.salon_mgt_backend.configs.billing.BillingProviderFactory;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.services.BillingProvider;
import com.panda.salon_mgt_backend.services.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing/webhook")
@RequiredArgsConstructor
public class BillingWebhookController {

    private final BillingProviderFactory providerFactory;
    private final BillingService billingService;

    @PostMapping("/{provider}")
    public ResponseEntity<Void> handleWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature
    ){
        BillingProvider billingProvider = providerFactory.get();

        if (!billingProvider.verifySignature(payload, signature)) {
            throw new SecurityException("Invalid webhook signature");
        }
        BillingResult result = billingProvider.verifyPayment(payload, signature);
        billingService.handlePaymentResult(result);
        return ResponseEntity.ok().build();
    }
}
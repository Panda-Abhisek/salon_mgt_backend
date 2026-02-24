package com.panda.salon_mgt_backend.controllers;

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

    private final BillingProvider billingProvider;
    private final BillingService billingService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Signature", required = false) String signature
    ) {
        BillingResult result =
                billingProvider.verifyPayment(payload, signature);

        billingService.handlePaymentResult(result);

        return ResponseEntity.ok().build();
    }
}
package com.panda.salon_mgt_backend.payloads;

public record BillingResult(
        String txId,                // 🔥 internal transaction ID
        String externalPaymentId,  // Stripe payment intent
        boolean success,
        boolean ignored
) {
    public boolean isIgnored() {
        return ignored;
    }
}
package com.panda.salon_mgt_backend.payloads;

public record BillingResult(
        String txId,
        String externalPaymentId,
        String stripeCustomerId,
        String stripeSubscriptionId,
        String stripeEventId,
        boolean success,
        boolean ignored
) {
    public boolean isIgnored() {
        return ignored;
    }
}
package com.panda.salon_mgt_backend.payloads;

public record BillingResult(
        String externalOrderId,
        String externalPaymentId,
        boolean success
) {}
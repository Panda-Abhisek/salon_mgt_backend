package com.panda.salon_mgt_backend.payloads;

public record CheckoutSession(
        String checkoutUrl,
        String externalOrderId
) {}
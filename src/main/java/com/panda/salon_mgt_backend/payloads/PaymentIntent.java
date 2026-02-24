package com.panda.salon_mgt_backend.payloads;

import com.panda.salon_mgt_backend.models.BillingTransaction;

public record PaymentIntent(
        BillingTransaction tx,
        String checkoutUrl
) {}
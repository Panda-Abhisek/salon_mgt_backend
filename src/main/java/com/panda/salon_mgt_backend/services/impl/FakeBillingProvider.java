package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.models.BillingProviderType;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.payloads.CheckoutSession;
import com.panda.salon_mgt_backend.services.BillingProvider;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FakeBillingProvider implements BillingProvider {

    @Override
    public BillingProviderType name() {
        return BillingProviderType.FAKE;
    }

    @Override
    public CheckoutSession createCheckout(
            Salon salon,
            Plan plan,
            BillingTransaction tx
    ) {
        String fakeOrderId = "fake_" + UUID.randomUUID();

        return new CheckoutSession(
                "http://localhost:5173/fake-success?orderId=" + fakeOrderId,
                fakeOrderId
        );
    }

    @Override
    public BillingResult verifyPayment(String payload, String signature) {
        return new BillingResult(
                payload,
                "fake_payment",
                true, false
        );
    }

//    @Override
//    public boolean verifySignature(String payload, String signature) {
//        return false; // fake world, fake security
//    }
}
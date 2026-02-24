package com.panda.salon_mgt_backend.configs.billing;

import com.panda.salon_mgt_backend.models.BillingProviderType;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.payloads.CheckoutSession;
import com.panda.salon_mgt_backend.services.BillingProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RazorpayBillingProvider implements BillingProvider {

    @Override
    public BillingProviderType name() {
        return BillingProviderType.RAZORPAY;
    }

    @Override
    public CheckoutSession createCheckout(Salon salon, Plan plan, BillingTransaction tx) {
        throw new UnsupportedOperationException("Razorpay not implemented yet");
    }

    @Override
    public BillingResult verifyPayment(String payload, String signature) {
        throw new UnsupportedOperationException("Razorpay not implemented yet");
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        return false;
    }
}
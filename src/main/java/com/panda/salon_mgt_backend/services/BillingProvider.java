package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.BillingProviderType;
import com.panda.salon_mgt_backend.models.BillingTransaction;
import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.payloads.CheckoutSession;

public interface BillingProvider {

    BillingProviderType name(); // FAKE / RAZORPAY / STRIPE

//    boolean verifySignature(String payload, String signature);

    CheckoutSession createCheckout(
            Salon salon,
            Plan plan,
            BillingTransaction tx
    );

    BillingResult verifyPayment(String payload, String signature);
}
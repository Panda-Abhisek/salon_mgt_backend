package com.panda.salon_mgt_backend.services;

import com.panda.salon_mgt_backend.models.Plan;
import com.panda.salon_mgt_backend.payloads.BillingResult;
import com.panda.salon_mgt_backend.payloads.PaymentIntent;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

public interface BillingService {
    @Transactional
    void handlePaymentResult(BillingResult result);

    PaymentIntent createPayment(Authentication auth, Plan newPlan);
}
package com.panda.salon_mgt_backend.services.impl;

import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.utils.TenantContext;
import com.stripe.model.billingportal.Session;
import com.stripe.param.billingportal.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.panda.salon_mgt_backend.models.SubscriptionStatus.*;

@Service
@RequiredArgsConstructor
public class BillingPortalService {

    private final TenantContext tenantContext;
    private final SubscriptionRepository subscriptionRepository;

    public String createPortalSession(Authentication auth) {

        var salon = tenantContext.getSalon(auth);

        Subscription sub = subscriptionRepository
                .findTopBySalonAndStatusInOrderByStartDateDesc(
                        salon,
                        List.of(ACTIVE, GRACE)
                )
                .orElseThrow(() -> new IllegalStateException("No active subscription"));

        if (sub.getStripeCustomerId() == null) {
            throw new IllegalStateException("No Stripe customer linked");
        }

        try {
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .setCustomer(sub.getStripeCustomerId())
                            .setReturnUrl("http://localhost:5173/billing")
                            .build();

            Session session = Session.create(params);
            return session.getUrl();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create billing portal session", e);
        }
    }
}
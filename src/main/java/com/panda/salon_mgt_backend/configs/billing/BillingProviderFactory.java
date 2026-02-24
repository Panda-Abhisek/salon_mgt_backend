package com.panda.salon_mgt_backend.configs.billing;

import com.panda.salon_mgt_backend.models.BillingProviderType;
import com.panda.salon_mgt_backend.services.BillingProvider;
import com.panda.salon_mgt_backend.services.impl.FakeBillingProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BillingProviderFactory {

    private final FakeBillingProvider fake;
    private final RazorpayBillingProvider razorpay;
    private final StripeBillingProvider stripe;

    @Value("${billing.provider:FAKE}")
    private String provider;

    public BillingProvider get() {
        BillingProviderType type = BillingProviderType.valueOf(provider);

        return switch (type) {
            case FAKE -> fake;
            case RAZORPAY -> razorpay;
            case STRIPE -> stripe;
        };
    }
}
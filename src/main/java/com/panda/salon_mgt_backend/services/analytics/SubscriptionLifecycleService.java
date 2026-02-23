package com.panda.salon_mgt_backend.services.analytics;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;
import com.panda.salon_mgt_backend.payloads.SubscriptionLifecycleResponse;
import com.panda.salon_mgt_backend.repositories.SubscriptionRepository;
import com.panda.salon_mgt_backend.utils.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionLifecycleService {

    private static final int URGENCY_DAYS = 3;
    private static final int GRACE_WINDOW_DAYS = 7;

    private final TenantContext tenantContext;
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionLifecycleResponse getLifecycle(Authentication auth) {

        Salon salon = tenantContext.getSalon(auth);

        Subscription sub = subscriptionRepository
                .findTopBySalonAndStatusInOrderByStartDateDesc(
                        salon,
                        List.of(
                                SubscriptionStatus.TRIAL,
                                SubscriptionStatus.ACTIVE,
                                SubscriptionStatus.GRACE
                        )
                )
                .orElseThrow(() -> new IllegalStateException("No subscription found"));

        SubscriptionStatus status = sub.getStatus();
        Instant now = Instant.now();

        long daysRemaining = 0;
        boolean endingSoon = false;

        if (sub.getEndDate() != null) {
            long days = Duration.between(now, sub.getEndDate()).toDays();
            daysRemaining = Math.max(days, 0);

            endingSoon = daysRemaining <= URGENCY_DAYS;
        }

        boolean inGrace = status == SubscriptionStatus.GRACE;
        boolean inTrial = status == SubscriptionStatus.TRIAL;

        return new SubscriptionLifecycleResponse(
                status,
                daysRemaining,
                endingSoon,
                inGrace,
                inTrial
        );
    }
}
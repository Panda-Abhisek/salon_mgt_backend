package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeWebhookEventRepository
        extends JpaRepository<StripeWebhookEvent, Long> {

    boolean existsByStripeEventId(String stripeEventId);
}
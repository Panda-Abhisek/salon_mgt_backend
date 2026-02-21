package com.panda.salon_mgt_backend.repositories;

import com.panda.salon_mgt_backend.models.Salon;
import com.panda.salon_mgt_backend.models.Subscription;
import com.panda.salon_mgt_backend.models.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findBySalonAndStatus(Salon salon, SubscriptionStatus subscriptionStatus);
}